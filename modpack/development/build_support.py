"""Suporte de build Windows: somente leitura no CurseForge, cache no projeto."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import urllib.request
import zipfile

ROOT = Path(__file__).resolve().parents[2]
CACHE = ROOT / 'modpack/.cache'
INSTALL = Path.home() / 'curseforge/minecraft/Install'


def jdk_home(explicit=None):
    candidates = [explicit] if explicit else [
        os.environ.get('JAVA_HOME'),
        *INSTALL.glob('runtime/java-runtime-delta/*/*'),
        *Path.home().glob('.jdks/*'),
    ]
    found = shutil.which('javac')
    if not explicit and found:
        candidates.append(Path(found).resolve().parent.parent)
    for candidate in filter(None, candidates):
        home = Path(candidate).resolve()
        compiler = home / ('bin/javac.exe' if os.name == 'nt' else 'bin/javac')
        if not compiler.is_file():
            continue
        result = subprocess.run([str(compiler), '-version'], capture_output=True, text=True, timeout=20)
        match = re.search(r'javac (\d+)', result.stdout + result.stderr)
        if result.returncode == 0 and match and int(match[1]) >= 21:
            print('JDK real:', home, (result.stdout + result.stderr).strip())
            return home
    raise RuntimeError('JDK 21+ completo não encontrado. Informe --jdk com bin/javac.exe funcional.')


def curseforge_libraries(install=INSTALL):
    """Seleciona apenas coordenadas de 1.21.1/21.1.263, jamais rglob global."""
    install = Path(install)
    artifacts = {}
    for version in ('1.21.1', 'neoforge-21.1.263'):
        metadata = json.loads((install / f'versions/{version}/{version}.json').read_text())
        if metadata['id'] != version:
            raise RuntimeError('Versão inesperada no metadata: ' + version)
        for lib in metadata['libraries']:
            artifact = (lib.get('downloads') or {}).get('artifact')
            if not artifact or not artifact['path'].endswith('.jar'):
                continue
            # Bibliotecas nativas não são APIs de compilação Java.
            if 'natives-' in artifact['path'] or 'native-epoll' in artifact['path'] or lib['name'].startswith('ca.weblite:'):
                continue
            coordinate = lib['name'].split(':')
            artifacts[tuple(coordinate[:2] + coordinate[3:])] = artifact
    libraries = []
    for artifact in artifacts.values():
        relative = Path(artifact['path'])
        if relative.is_absolute() or '..' in relative.parts:
            raise RuntimeError('Caminho de biblioteca inválido')
        path = install / 'libraries' / relative
        expected = artifact.get('sha1')
        if not path.is_file():
            path = CACHE / 'build-libraries' / relative
            if not path.is_file():
                path.parent.mkdir(parents=True, exist_ok=True)
                with urllib.request.urlopen(artifact['url'], timeout=60) as response:
                    content = response.read()
                if not expected or hashlib.sha1(content).hexdigest() != expected:
                    raise RuntimeError('SHA-1 inválido no download: ' + str(relative))
                path.write_bytes(content)
        if expected and hashlib.sha1(path.read_bytes()).hexdigest() != expected:
            raise RuntimeError('Biblioteca local diverge do metadata: ' + str(path))
        libraries.append(path)
    # Classes Mojmap transformadas pelo NeoForge precedem as classes vanilla.
    for relative in (
        'net/neoforged/neoforge/21.1.263/neoforge-21.1.263-universal.jar',
        'net/neoforged/neoforge/21.1.263/neoforge-21.1.263-client.jar',
        'net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar',
    ):
        path = install / 'libraries' / relative
        if not path.is_file():
            raise RuntimeError('Artefato Mojmap gerado pelo launcher ausente: ' + str(path))
        libraries.insert(0, path)
    libraries.sort(key=lambda p: (0 if p.name == 'neoforge-21.1.263-client.jar' else 1 if p.name.endswith('-srg.jar') else 2))
    if not any(p.name == 'neoforge-21.1.263-universal.jar' for p in libraries):
        raise RuntimeError('NeoForge universal 21.1.263 ausente')
    return list(dict.fromkeys(libraries))


def server_libraries(path):
    libraries = [p for p in sorted(Path(path).rglob('*.jar')) if not p.name.endswith(('-slim.jar', '-unpacked.jar'))]
    libraries.sort(key=lambda p: (0 if p.name == 'neoforge-21.1.263-server.jar' else 1, str(p)))
    if not any(p.name == 'neoforge-21.1.263-universal.jar' for p in libraries):
        raise RuntimeError('Instalação de servidor NeoForge 21.1.263 incompleta: ' + str(path))
    return libraries


def mod_apis(prefixes):
    """Extrai somente APIs necessárias do ZIP, incluindo o engine JarJar Exotic Worlds."""
    import io
    result = []
    directory = CACHE / 'integration-apis'
    directory.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(ROOT / 'modpack/mods.zip') as archive:
        for prefix in prefixes:
            names = [n for n in archive.namelist() if Path(n).name.startswith(prefix) and n.endswith('.jar')]
            if len(names) != 1:
                raise RuntimeError(f'API {prefix}: esperado um JAR no ZIP, encontrados {names}')
            name = names[0]
            data = archive.read(name)
            target = directory / Path(name).name
            target.write_bytes(data)
            result.append(target)
            if prefix == 'exotic_worlds-':
                with zipfile.ZipFile(io.BytesIO(data)) as bundle:
                    for nested in bundle.namelist():
                        if nested.startswith('META-INF/jarjar/exotic_worlds-neoforge-') and nested.endswith('.jar'):
                            target = directory / Path(nested).name
                            target.write_bytes(bundle.read(nested))
                            result.append(target)
    return result


def compile_java(jdk, libraries, sources, output, argfile):
    # Diretório novo a cada execução impede classes antigas de entrarem no JAR.
    import tempfile
    output.mkdir(parents=True, exist_ok=True)
    classes = Path(tempfile.mkdtemp(prefix='classes-', dir=output))
    argv = ['--release', '21', '-proc:none', '-encoding', 'UTF-8', '-classpath', os.pathsep.join(str(p.resolve()) for p in libraries), '-d', str(classes), *map(str, sources)]
    argfile.write_text('\n'.join('"' + s.replace('\\', '/') + '"' for s in argv), encoding='utf-8')
    subprocess.run([str(jdk / ('bin/javac.exe' if os.name == 'nt' else 'bin/javac')), '@' + str(argfile)], check=True)
    return classes

# Modpack — área versionada

Use `overrides/` para guardar **somente** configs, scripts, assets e outros arquivos personalizados necessários para o perfil. JARs de mods devem ser gerenciados pelo launcher/manifest e pelas políticas de distribuição.

- `overrides/kubejs/startup_scripts/`: registros executados no startup.
- `overrides/kubejs/server_scripts/`: receitas, eventos e lógica de servidor.
- `overrides/kubejs/client_scripts/`: mudanças específicas do cliente.
- `overrides/config/`: configs globais versionadas.
- `overrides/defaultconfigs/`: configs padrão destinadas a mundos novos.

Os arquivos `.gitkeep` apenas preservam pastas vazias no Git.

## Exportação CurseForge

A saída oficial do launcher contém `manifest.json` e `overrides/` no topo do ZIP. **Esta pasta do repositório não é, sozinha, um ZIP de instalação pronto.** Gere o manifest pelo perfil testado e compare os overrides exportados com os versionados.

"""Lê SOMENTE WorldGenSettings de level.dat; nunca escreve no save.

Saída exclui nome do mundo, seed, jogador, posição e inventário pessoais.
O nome do preset não é persistido como prova: os geradores efetivos são a evidência.
"""
import argparse
import gzip
import hashlib
import io
import json
from pathlib import Path
import struct


def read_nbt(data):
    stream=io.BytesIO(data)
    def number(fmt): return struct.unpack('>'+fmt,stream.read(struct.calcsize('>'+fmt)))[0]
    def string(): return stream.read(number('H')).decode('utf-8',errors='replace')
    def payload(kind):
        if kind in range(1,7):return number({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[kind])
        if kind==7:return stream.read(number('i'))
        if kind==8:return string()
        if kind==9:
            child=number('B');length=number('i');return [payload(child) for _ in range(length)]
        if kind==10:
            result={}
            while (child:=number('B'))!=0:
                name=string();result[name]=payload(child)
            return result
        if kind in (11,12):return [number('i' if kind==11 else 'q') for _ in range(number('i'))]
        raise ValueError('Tag NBT desconhecida: '+str(kind))
    root=number('B');string();return payload(root)


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--save',type=Path,required=True,help='level.dat ou diretório de save, somente leitura.')
    parser.add_argument('--output',type=Path,required=True,help='JSON novo fora do save.')
    args=parser.parse_args()
    source=args.save/'level.dat' if args.save.is_dir() else args.save
    if args.output.resolve().is_relative_to(source.parent.resolve()) or args.output.exists():
        parser.error('Saída deve ser nova e fora do save.')
    before=source.read_bytes()
    data=read_nbt(gzip.decompress(before))['Data']
    dimensions={}
    for key,stem in data['WorldGenSettings']['dimensions'].items():
        gen=stem['generator']
        dimensions[key]={k:gen[k] for k in ('type','settings','wrapping','climate_compression',
            'exotic_worlds:wrapping','exotic_worlds:climate_compression') if k in gen}
        dimensions[key]['biome_source_type']=gen.get('biome_source',{}).get('type')
        if isinstance(dimensions[key].get('settings'),dict):
            inline=dimensions[key].pop('settings')
            dimensions[key]['inline_settings_sha256']=hashlib.sha256(json.dumps(inline,sort_keys=True).encode()).hexdigest()
    assert source.read_bytes()==before,'Save mudou durante leitura; não use como snapshot estável.'
    report=dict(state='read_only_saved_metadata',level_dat_sha256=hashlib.sha256(before).hexdigest(),
        data_version=data.get('DataVersion'),dimensions=dimensions,
        note='Nenhum dado pessoal exportado. Geradores salvos não identificam por si só o preset escolhido nem provam runtime; nenhuma alteração no save.')
    args.output.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(args.output)
    print(json.dumps(dimensions,ensure_ascii=False))


if __name__=='__main__':main()

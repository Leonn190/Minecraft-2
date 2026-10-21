# Portais físicos Pokémon e Arcanus

Implementação para **BBL Portals 1.2.2**, KubeJS `2101.7.2-build.377`, Undergarden `0.9.6`, Minecraft 1.21.1 / NeoForge 21.1.263. Reinício completo obrigatório. Os arquivos fazem parte do staging de teste descrito em [VALIDACAO_WORLDGEN.md](VALIDACAO_WORLDGEN.md); a instância ativa e os saves existentes não foram alterados.

## Construção e uso

Ambos são retângulos verticais nos eixos X ou Z, com interior de **2–21 blocos de largura e 3–21 de altura**, sem obstruções. Todos os cantos são obrigatórios. Clique em uma face da moldura voltada para o interior com **`undergarden:catalyst`**. O item não é consumido nem danificado. Flint and Steel não ativa esses portais.

Pokémon: ferro na metade inferior da altura externa, redstone na superior; quando a altura externa é ímpar, a linha central pertence à parte superior. Exemplo mínimo externo 4×5 (`I` ferro, `R` redstone, `.` interior):

```text
RRRR
R..R
R..R
I..I
IIII
```

Para duas metades de três linhas cada, use altura externa 6 / interior 4:

```text
RRRR
R..R
R..R
I..I
I..I
IIII
```

Destino **`m2pokemon:world`**, cor vermelha RGB (220, 35, 45). Misturas aleatórias, metades invertidas e cantos de material errado são rejeitados.

Arcanus: todos os blocos da moldura de **`minecraft:crying_obsidian`**, incluindo os cantos. Destino **`m2arcana:world`**, cor roxa RGB (155, 60, 230).

Os pares ligam Overworld ↔ seu respectivo mundo; a ignição fica restrita a esses dois lados. O transporte e o armazenamento persistente de links bidirecionais são do BBL. A primeira travessia cria uma moldura válida no destino, com área livre e plataforma dos dois lados. O retorno usa o link correspondente enquanto a moldura permanecer válida. A integração aplica o cooldown de portal vanilla após a viagem. Não mantém estado global por jogador nem realiza teletransportes por tick ou comandos.

Se a moldura vinculada for destruída ou obstruída, o BBL pode criar outra. Não se promete reencontrar um portal arbitrário construído anteriormente sem vínculo. As viagens devem ser testadas também perto das costuras toroidais antes de usar um mundo definitivo.

## API e código

- `modpack/kubejs/startup_scripts/dimension_portals.js`: registro real em `PortalEvents.register`, com `event.create()`, `frameBlock`, **`lightWithItem`**, `setDestination`, `returnDim` e `tint`.
- `integration/src/main/java/br/com/minecraft2/portal/PortalIntegration.java`: recebe `PortalMaker` e configura seu `CustomPortalBuilder` por Java. O campo `builder` existe em 1.2.2, mas é `@HideFromJS`; não é acessado pelo script.
- `integration/src/main/java/br/com/minecraft2/portal/DimensionPortalFrame.java`: implementação própria que estende o helper vertical do BBL. Substitui a validação de posições, a criação do portal de destino e a fábrica de novos validadores. Esta última substituição é necessária porque o helper padrão instancia sua própria classe e perderia a validação da subclasse.
- Ferro e redstone têm registros para o mesmo destino/validador; isso permite clicar na parte superior e permite ao BBL identificar corretamente o portal pela borda mais próxima. Não há registros para obsidiana vanilla nem para os blocos da moldura Undergarden.

O BBL 1.2.2 usa `RightClickBlock` no servidor para acender com item e cancela a interação quando reconhece um portal. Não reduz o stack. Se a moldura não corresponder, a interação continua e `CatalystItem.useOn` original do Undergarden permanece responsável pelo seu portal. Nenhum mixin altera esse item ou o portal original.

Fontes: [wiki KubeJS oficial](https://github.com/benbenlaw/BBL-Portals/wiki/KubeJS), [wiki Java oficial](https://github.com/benbenlaw/BBL-Portals/wiki/Java), inspeção das classes dos JARs exatos registrados no manifesto. O Java é compilado contra essas APIs locais; não utiliza nomes supostos de métodos.

## Validação

O mod de testes separado contém `PortalSmokeTest`: constrói blocos reais, publica eventos de interação com `FakePlayer`, usa o Catalyst original e realiza viagens de entidade pelo `DimensionTransition` do BBL. Os resultados finais ficam na seção `portals` de [worldgen-runtime-validation.json](worldgen-runtime-validation.json). Uma falha interrompe a aprovação do relatório geral.

Isso cobre integração de servidor, sem substituir teste visual com cliente, conexão de dois jogadores e persistência após reinício. Checklist com cliente em **mundo novo**:

1. Construir os dois exemplos Pokémon e Arcanus em ambos os eixos. Acender embaixo, em cima e pelas laterais com o mesmo Catalyst.
2. Rejeitar moldura incompleta, canto errado, metades trocadas, mistura de materiais, interior bloqueado, tamanho abaixo/acima dos limites e Flint and Steel.
3. Conferir cores, tempo de entrada, saída segura, ida e retorno, entrar novamente após sair e aguardar o cooldown.
4. Dois jogadores cruzam portais distintos e o mesmo portal; nenhum recebe o vínculo de outro. Sair do servidor, reiniciar e conferir os links persistidos.
5. Acender o Undergarden com Catalyst e o Nether com Flint and Steel, conferir ida/volta e coordenadas perto das bordas.
6. Manter `/trigger` administrativos existentes e confirmar que equipamentos e invocações manuais continuam disponíveis nas outras dimensões.

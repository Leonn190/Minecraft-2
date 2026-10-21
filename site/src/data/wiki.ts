/**
 * Conteúdo editorial manual da wiki do Minecraft 2.
 * Revisar descrições e estados antes de cada distribuição pública do modpack.
 * Não é gerado automaticamente a partir do modlist.
 */
export type WikiCategory = 'dimensoes' | 'mecanicas' | 'aventura';
export type WikiStatus = 'Em planejamento' | 'Em desenvolvimento' | 'Em validação';
export interface WikiArticle {
  slug: string;
  title: string;
  category: WikiCategory;
  eyebrow: string;
  summary: string;
  introduction: string;
  status: WikiStatus;
  accent: string;
  artwork: string;
  facts: { label: string; value: string }[];
  sections: { id: string; title: string; paragraphs: string[]; bullets?: string[]; note?: string }[];
  related: string[];
  featured?: boolean;
}
export const categories: { id: WikiCategory | 'todas'; label: string; description: string }[] = [
  { id: 'todas', label: 'Tudo', description: 'Toda a enciclopédia' },
  { id: 'dimensoes', label: 'Dimensões', description: 'Mundos, acessos e perigos' },
  { id: 'mecanicas', label: 'Mecânicas', description: 'Regras e sistemas exclusivos' },
  { id: 'aventura', label: 'Aventura', description: 'Exploração, combate e sobrevivência' },
];
export const articles: WikiArticle[] = [
  {
    slug: 'mundo-principal', title: 'Mundo Principal', category: 'dimensoes', eyebrow: '01 / O início de tudo',
    summary: 'O ponto de partida da aventura: bases, engenharia e o primeiro contato com o Minecraft 2.',
    introduction: 'O Overworld continua sendo a base da sua jornada. A proposta é transformá-lo em um mundo rico em biomas, estruturas, criaturas e possibilidades de construção, mantendo as principais mudanças dimensionais em regiões próprias.',
    status: 'Em desenvolvimento', accent: '#c7b78e', artwork: 'overworld', featured: true,
    facts: [{label:'Acesso',value:'Início do jogo'},{label:'Papel',value:'Exploração e construção'},{label:'Biomas',value:'Oh The Biomes (planejado)'}],
    sections: [
      {id:'o-que-encontrar',title:'O que encontrar',paragraphs:['O Mundo Principal é o lugar para explorar biomas, reunir recursos, construir uma base e iniciar sistemas de engenharia. A diversidade de criaturas e estruturas deve fazer a exploração valer a pena mesmo depois de muitas horas.']},
      {id:'proposta',title:'Identidade da dimensão',paragraphs:['A ideia é reservar os Pokémon para a dimensão específica deles, enquanto magia avançada e determinados recursos especiais dependem de outros mundos. Essa separação é uma regra do projeto, ainda sujeita a testes.']},
      {id:'dicas',title:'Antes de sair explorando',paragraphs:['Estabeleça uma base, organize os recursos e garanta um modo seguro de retornar. O mundo do Minecraft 2 foi pensado para apresentar desafios gradualmente, mas viajar despreparado sempre será arriscado.'],bullets:['Tenha comida, ferramentas e equipamentos de reposição.','Marque a localização da base.','Confira na wiki como funcionam os portais antes de tentar atravessá-los.']},
    ], related: ['dimensao-pokemon','portais-personalizados','engenharia-create']
  },
  {
    slug: 'dimensao-pokemon', title: 'Dimensão Pokémon', category: 'dimensoes', eyebrow: '02 / Um mundo vivo',
    summary: 'Um mundo dedicado ao Cobblemon, à captura, ao treinamento e à exploração.',
    introduction: 'A região Pokémon é planejada como um mundo separado: sua própria natureza, espécies, encontros e oportunidades de aventura. Assim, a experiência de Cobblemon pode ter uma identidade forte sem dominar os outros mundos.',
    status: 'Em desenvolvimento', accent: '#94c7aa', artwork: 'pokemon', featured: true,
    facts: [{label:'Acesso',value:'Portal personalizado'},{label:'Terreno',value:'Terralith (planejado)'},{label:'Foco',value:'Cobblemon'}],
    sections: [
      {id:'acesso',title:'Como chegar',paragraphs:['O projeto prevê um portal com moldura especial de ferro e redstone, aceso com pederneira. A receita visual, a validação da estrutura e o destino final precisam ser conferidos na versão jogável.'],note:'O destino técnico previsto para o portal é m2pokemon:world.'},
      {id:'jogabilidade',title:'Como funciona',paragraphs:['A proposta é concentrar os Pokémon, seus encontros e a maior parte dos recursos específicos do Cobblemon nesta dimensão. Isso permite explorar, batalhar e construir uma equipe sem transformar o Mundo Principal num mapa Pokémon.']},
      {id:'cuidados',title:'Dicas para treinadores',paragraphs:['Planeje suas expedições com itens suficientes para vários encontros, mantenha espaço no inventário e faça uma rota segura de retorno. As regras finais de encontro, captura e spawn ainda dependem de testes.']}
    ],related:['cobblemon','portais-personalizados','mundo-principal']
  },
  {
    slug:'arcanus',title:'Arcanus',category:'dimensoes',eyebrow:'03 / O domínio arcano',
    summary:'O reino da magia, dos rituais e das possibilidades arcanas.',
    introduction:'Arcanus é pensado como o centro da progressão mágica do modpack. A intenção é que sistemas de feitiços e materiais arcanos tenham sentido dentro de um lugar próprio, e não sejam simplesmente mais uma ferramenta disponível em toda parte.',
    status:'Em planejamento',accent:'#b7a3e6',artwork:'arcanus',featured:true,
    facts:[{label:'Acesso',value:'Portal personalizado'},{label:'Base mágica',value:'Ars Nouveau'},{label:'Mundo',value:'Geração em estudo'}],
    sections:[
      {id:'portal',title:'Entrada no reino',paragraphs:['Está previsto um portal semelhante ao do Nether usando obsidiana chorosa. O acendimento usaria pederneira e o destino desejado é m2arcana:world. A implementação final será confirmada depois dos testes.']},
      {id:'magia',title:'Magia com identidade própria',paragraphs:['O plano é concentrar sistemas de Ars Nouveau e seus complementos nessa dimensão, impondo restrições fora dela quando necessário. Isso ajuda a equilibrar magia, aventura e engenharia.']},
      {id:'equilibrio',title:'Uma progressão controlada',paragraphs:['A disponibilidade dos feitiços, materiais e equipamentos mais poderosos deverá ser ajustada para não eliminar a importância das outras dimensões. O conjunto final de addons e suas restrições ainda está em avaliação.']}
    ],related:['magia-arcanus','portais-personalizados','undergarden']
  },
  {
    slug:'undergarden',title:'Undergarden',category:'dimensoes',eyebrow:'04 / Abaixo de tudo',
    summary:'Um mundo subterrâneo hostil onde o equipamento de fora não é tão confiável.',
    introduction:'Undergarden oferece uma identidade de sobrevivência própria: ambientes sombrios, materiais incomuns e criaturas perigosas. O Minecraft 2 pretende incentivar o uso de equipamentos da própria dimensão.',
    status:'Em desenvolvimento',accent:'#a3c8b2',artwork:'undergarden',featured:true,
    facts:[{label:'Ambiente',value:'Subterrâneo e hostil'},{label:'Foco',value:'Equipamento local'},{label:'Regra prevista',value:'−60% dano externo'}],
    sections:[
      {id:'desafio',title:'Por que é diferente',paragraphs:['A proposta é que o Undergarden não seja apenas mais um lugar para visitar e coletar recursos. Entrar nele exige adaptação ao ambiente e atenção aos inimigos.']},
      {id:'armas',title:'Armas de outras dimensões',paragraphs:['Uma regra personalizada prevista para o modpack reduz em 60% o dano de armas que não são do próprio Undergarden quando usadas lá. Isso valoriza a exploração e os equipamentos locais.'],note:'Regra de balanceamento planejada: só considere ativa depois de confirmação em testes.'},
      {id:'preparacao',title:'Como se preparar',paragraphs:['Carregue suprimentos, marque o retorno e procure cedo materiais capazes de melhorar sua eficiência de combate dentro da dimensão. Não dependa apenas do dano que suas armas causam em outros mundos.']}
    ],related:['regras-de-combate','preparacao-de-expedicoes','arcanus']
  },
  {
    slug:'backwoods',title:'Backwoods',category:'dimensoes',eyebrow:'05 / O pesadelo',
    summary:'Um ambiente estranho idealizado para encontros inesperados durante o sono.',
    introduction:'Backwoods foi considerado para representar uma dimensão de pesadelo dentro do Minecraft 2. A intenção é que dormir nem sempre signifique estar completamente seguro — sem transformar essa ideia numa punição constante.',
    status:'Em planejamento',accent:'#d1a98f',artwork:'backwoods',
    facts:[{label:'Tema',value:'Pesadelo'},{label:'Acesso planejado',value:'Evento ao dormir'},{label:'Estado',value:'Mecânica a validar'}],
    sections:[
      {id:'conceito',title:'Conceito',paragraphs:['O Backwoods teria uma atmosfera de terror e exploração diferente das demais dimensões, com riscos e recompensas próprias.']},
      {id:'sono',title:'O evento de sono',paragraphs:['A possibilidade de ser levado ao pesadelo ao dormir é uma ideia do projeto, não uma mecânica confirmada. A frequência, as condições de retorno e as consequências precisarão ser calibradas com cuidado.']},
      {id:'equilibrio',title:'Sem frustração gratuita',paragraphs:['O objetivo é criar suspense e surpresa, oferecendo meios compreensíveis para escapar, em vez de interromper constantemente a progressão normal do jogador.']}
    ],related:['preparacao-de-expedicoes','macabre']
  },
  {
    slug:'macabre',title:'Macabre',category:'dimensoes',eyebrow:'06 / Além do véu',
    summary:'Um limbo sombrio dentro da proposta de aventura e terror do projeto.',
    introduction:'Macabre aparece no planejamento como um espaço ligado ao limbo e ao sobrenatural. Sua função final na jornada ainda será definida, respeitando o equilíbrio entre terror, exploração e descoberta.',
    status:'Em planejamento',accent:'#b6a8cc',artwork:'macabre',
    facts:[{label:'Tema',value:'Limbo'},{label:'Foco',value:'Terror e exploração'},{label:'Acesso',value:'A definir'}],
    sections:[
      {id:'identidade',title:'Identidade do mundo',paragraphs:['A proposta é oferecer uma experiência sombria e distinta, sem repetir exatamente o papel de Backwoods ou Undergarden.']},
      {id:'conteudo',title:'Conteúdo e ameaças',paragraphs:['Criaturas, recursos, encontros e formas de acesso dependem da configuração final do modpack. Esta página será aprofundada quando esse conteúdo estiver testado.']}
    ],related:['backwoods','carne','preparacao-de-expedicoes']
  },
  {
    slug:'carne',title:'Dimensão da Carne',category:'dimensoes',eyebrow:'07 / O orgânico impossível',
    summary:'Uma proposta de mundo grotesco inspirado em horror corporal.',
    introduction:'A Dimensão da Carne é planejada como um desafio intenso e visualmente perturbador, com uma identidade orgânica bastante diferente de todos os outros mundos.',
    status:'Em planejamento',accent:'#de8d85',artwork:'carne',
    facts:[{label:'Tema',value:'Horror orgânico'},{label:'Conteúdo previsto',value:'The Flesh That Hates'},{label:'Acesso',value:'A definir'}],
    sections:[
      {id:'proposta',title:'A proposta',paragraphs:['Essa dimensão reuniria ameaças e materiais relacionados ao universo orgânico do mod, criando um dos ambientes mais perigosos do Minecraft 2.']},
      {id:'estado',title:'O que já está definido',paragraphs:['Seu tema geral está definido, mas acesso, geração, criaturas, recompensas e progressão ainda precisam de validação técnica. O conteúdo apresentado aqui é intenção de design.']}
    ],related:['macabre','regras-de-combate']
  },
  {
    slug:'nether-e-end',title:'Nether e End',category:'dimensoes',eyebrow:'08 / Os clássicos reinventados',
    summary:'Os mundos clássicos continuam importantes, com novos desafios e oportunidades.',
    introduction:'Nether e End fazem parte da identidade do Minecraft. No Minecraft 2, a proposta é ampliá-los com biomas, ameaças e novas razões para explorar, sem apagar o papel original dessas dimensões.',
    status:'Em desenvolvimento',accent:'#d4a1a1',artwork:'nether',
    facts:[{label:'Mundos',value:'Nether e End'},{label:'Foco',value:'Exploração e desafios'},{label:'Mudanças',value:'Dependem da versão'}],
    sections:[
      {id:'nether',title:'Nether',paragraphs:['A proposta é ampliar a diversidade de ambientes e a sensação de perigo. Modificações de geração e balanceamento serão revisadas antes do lançamento.']},
      {id:'end',title:'End',paragraphs:['O End deve continuar sendo uma etapa importante da aventura, com conteúdo para quem quer continuar explorando após os desafios tradicionais.']}
    ],related:['mundo-principal','preparacao-de-expedicoes']
  },
  {
    slug:'portais-personalizados',title:'Portais Personalizados',category:'mecanicas',eyebrow:'Sistema / Entre mundos',
    summary:'Entradas especiais que tornam a viagem entre dimensões uma descoberta.',
    introduction:'Em vez de depender apenas de comandos, o Minecraft 2 pretende permitir que o jogador construa e ative portais físicos para alguns dos mundos personalizados.',
    status:'Em desenvolvimento',accent:'#c9b999',artwork:'portals',featured:true,
    facts:[{label:'Ativação prevista',value:'Pederneira'},{label:'Sistema estudado',value:'BBL Portals'},{label:'Dimensões',value:'Pokémon e Arcanus'}],
    sections:[
      {id:'pokemon',title:'Portal Pokémon',paragraphs:['O projeto prevê uma moldura retangular no estilo do portal do Nether. O topo seria construído com blocos de redstone, a base com blocos de ferro, e as laterais combinariam ambos os materiais por altura.']},
      {id:'arcanus',title:'Portal Arcanus',paragraphs:['Para Arcanus, a moldura prevista utiliza obsidiana chorosa. Como no portal Pokémon, a intenção é que a ativação ocorra com pederneira.']},
      {id:'validacao',title:'Situação da mecânica',paragraphs:['Construção, orientação, teleporte de retorno e validação dos formatos devem ser testados em jogo antes de publicar um tutorial definitivo.'],note:'Esta página descreve um design em desenvolvimento; não garante que os portais já estejam funcionais na versão baixada.'}
    ],related:['dimensao-pokemon','arcanus','preparacao-de-expedicoes']
  },
  {
    slug:'cobblemon',title:'Cobblemon no Minecraft 2',category:'mecanicas',eyebrow:'Sistema / Criaturas',
    summary:'Uma experiência Pokémon concentrada num mundo próprio.',
    introduction:'Cobblemon adiciona uma camada inteira de aventura ao Minecraft 2. A decisão de design é reservar essa experiência à dimensão Pokémon, com seus próprios ambientes e regras.',
    status:'Em desenvolvimento',accent:'#98c8b1',artwork:'pokemon',
    facts:[{label:'Sistema-base',value:'Cobblemon'},{label:'Local',value:'Dimensão Pokémon'},{label:'Captura',value:'Conforme o mod e addons'}],
    sections:[
      {id:'exploracao',title:'Exploração e encontros',paragraphs:['Explorar o ambiente, encontrar espécies e construir uma equipe devem ser atividades próprias deste mundo. Os spawns reais dependem do conjunto de mods e da configuração final.']},
      {id:'separacao',title:'Separação entre mundos',paragraphs:['A proposta é impedir que Pokémon e vegetação específica apareçam indiscriminadamente no Mundo Principal. Isso ainda requer confirmação de testes.']},
      {id:'dicas',title:'O que levar',paragraphs:['Reserve espaço para itens, prepare ferramentas para viagens longas e estabeleça um caminho de retorno seguro ao portal.']}
    ],related:['dimensao-pokemon','portais-personalizados']
  },
  {
    slug:'magia-arcanus',title:'Magia e Arcanus',category:'mecanicas',eyebrow:'Sistema / O arcano',
    summary:'Feitiços e progressão mágica pensados para um ambiente exclusivo.',
    introduction:'A magia deve ter espaço e relevância, mas sem invalidar os outros estilos de jogo. Arcanus foi idealizada justamente para dar um lar próprio à progressão arcana.',
    status:'Em planejamento',accent:'#b6a0e2',artwork:'arcanus',
    facts:[{label:'Base',value:'Ars Nouveau'},{label:'Local prioritário',value:'Arcanus'},{label:'Restrições',value:'Em estudo'}],
    sections:[
      {id:'ideia',title:'Como a magia se encaixa',paragraphs:['Rituais, feitiços, recursos e equipamentos relacionados à magia ganhariam mais valor ao depender da exploração de Arcanus.']},
      {id:'limites',title:'Regras fora da dimensão',paragraphs:['O projeto prevê avaliar limitações para interfaces, itens e sistemas mágicos fora de Arcanus. Nenhuma restrição específica deve ser tratada como confirmada antes dos testes.']},
      {id:'integracao',title:'Equilíbrio com outros caminhos',paragraphs:['O objetivo não é fazer a magia vencer tudo: engenharia, equipamentos, exploração e combate devem continuar oferecendo escolhas válidas.']}
    ],related:['arcanus','engenharia-create','regras-de-combate']
  },
  {
    slug:'engenharia-create',title:'Engenharia e Create',category:'mecanicas',eyebrow:'Sistema / Construir',
    summary:'Mecanismos, movimento e automação como ferramentas criativas de sobrevivência.',
    introduction:'O Minecraft 2 também é sobre construir grandes projetos. Create e complementos aparecem no planejamento para ampliar as possibilidades de máquinas e bases, sem roubar a identidade da exploração.',
    status:'Em desenvolvimento',accent:'#c8b18f',artwork:'overworld',
    facts:[{label:'Base',value:'Create'},{label:'Estilo',value:'Engenharia e automação'},{label:'Papel',value:'Construção e utilidade'}],
    sections:[
      {id:'construcoes',title:'O que pode construir',paragraphs:['Sistemas mecânicos, transporte de itens, processamento de recursos e máquinas de diversos tipos podem reduzir trabalhos repetitivos e dar espaço à criatividade. O conteúdo específico depende dos addons instalados.']},
      {id:'equilibrio',title:'Papel na aventura',paragraphs:['Automatizar e construir deve facilitar sua vida, mas não substituir a necessidade de explorar outras dimensões em busca de recursos e desafios.']}
    ],related:['mundo-principal','magia-arcanus','preparacao-de-expedicoes']
  },
  {
    slug:'regras-de-combate',title:'Regras de Combate',category:'aventura',eyebrow:'Sobrevivência / Luta',
    summary:'Equipamentos, escolhas e regras que tornam cada mundo diferente.',
    introduction:'O equilíbrio do Minecraft 2 depende de impedir que uma única arma seja sempre a melhor resposta. Algumas dimensões terão regras próprias para incentivar variedade.',
    status:'Em desenvolvimento',accent:'#d2a390',artwork:'undergarden',
    facts:[{label:'Foco',value:'Balanceamento'},{label:'Regra especial',value:'Undergarden'},{label:'Estado',value:'A validar'}],
    sections:[
      {id:'penalidade',title:'A regra do Undergarden',paragraphs:['Foi solicitada uma redução de 60% no dano de armas que não pertencem ao Undergarden, quando utilizadas naquela dimensão. O objetivo é tornar as armas locais relevantes sem simplesmente proibir as demais.']},
      {id:'pratica',title:'Por que isso importa',paragraphs:['Uma arma poderosa no Mundo Principal pode não ser sua melhor opção no Undergarden. Explorar, obter equipamentos e adaptar sua estratégia devem ser partes importantes da progressão.']},
      {id:'teste',title:'Implementação e testes',paragraphs:['A regra precisa considerar armas de diferentes mods e todas as fontes relevantes de dano. A implementação ainda deve ser verificada em jogo.']}
    ],related:['undergarden','preparacao-de-expedicoes','magia-arcanus']
  },
  {
    slug:'preparacao-de-expedicoes',title:'Preparação para Expedições',category:'aventura',eyebrow:'Sobrevivência / Explorar',
    summary:'Como evitar que uma grande aventura termine por falta de planejamento.',
    introduction:'Múltiplas dimensões significam ambientes, perigos e regras diferentes. Preparar a mochila e o caminho de volta é quase tão importante quanto escolher uma arma.',
    status:'Em desenvolvimento',accent:'#b7c5b2',artwork:'overworld',
    facts:[{label:'Indicado para',value:'Todos os jogadores'},{label:'Tema',value:'Exploração'},{label:'Prioridade',value:'Sobreviver e retornar'}],
    sections:[
      {id:'antes',title:'Antes de atravessar',paragraphs:['Guarde uma cópia dos recursos importantes na base e leve um conjunto adequado de comida, luz e ferramentas. Não dependa de um único caminho de retorno.'],bullets:['Marque o portal ou a entrada.','Leve suprimentos para imprevistos.','Pesquise as particularidades da dimensão na wiki.','Evite carregar todos os itens valiosos em sua primeira visita.']},
      {id:'durante',title:'Durante a viagem',paragraphs:['Explore com atenção e adapte seu equipamento às ameaças. Em dimensões com regras especiais, como o Undergarden, suas estratégias habituais podem não funcionar.']},
      {id:'depois',title:'Ao retornar',paragraphs:['Organize novos materiais, teste equipamentos e planeje a próxima viagem com base no que descobriu. Uma expedição bem-sucedida também prepara a seguinte.']}
    ],related:['mundo-principal','portais-personalizados','regras-de-combate']
  },
  {
    slug:'limites-dos-mundos',title:'Limites dos Mundos',category:'mecanicas',eyebrow:'Sistema / Geração',
    summary:'Dimensões com escalas diferentes e exploração com limites planejados.',
    introduction:'O Minecraft 2 estuda mundos com limites distintos, para controlar geração, exploração e o custo de manter um servidor grande.',
    status:'Em planejamento',accent:'#c3b7a5',artwork:'overworld',
    facts:[{label:'Escala',value:'Varia por dimensão'},{label:'Sistema em estudo',value:'Exotic Worlds'},{label:'Mecânica',value:'Toroidal sob avaliação'}],
    sections:[
      {id:'objetivo',title:'Por que estabelecer limites',paragraphs:['Mundos bem delimitados podem ajudar a organizar regiões e administrar recursos do servidor, desde que a configuração respeite as particularidades de cada dimensão.']},
      {id:'toroidal',title:'Mundos toroidais',paragraphs:['A intenção é investigar limites que conectem extremidades, criando a sensação de uma superfície contínua. Um teleporte simples nas bordas não deve ser descrito como um mundo toroidal perfeito.'],note:'O comportamento real depende de compatibilidade e testes em mundos novos. Saves existentes não devem ser alterados sem suporte comprovado.'},
      {id:'tamanhos',title:'Escalas planejadas',paragraphs:['Os tamanhos de cada dimensão e as regras de transição ainda precisam ser consolidados. Esta página será atualizada manualmente quando existirem dados validados.']}
    ],related:['mundo-principal','dimensao-pokemon','portais-personalizados']
  }
];

export const getArticle = (slug: string) => articles.find(a => a.slug === slug);
export const getRelated = (article: WikiArticle) => article.related.map(getArticle).filter((a): a is WikiArticle => Boolean(a));
export const categoryLabel = (c: WikiCategory) => categories.find(x => x.id === c)?.label ?? 'Wiki';

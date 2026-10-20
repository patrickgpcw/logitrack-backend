# Plano de desenvolvimento — LogiTrack

## Objetivo e ritmo

Entregar primeiro uma demonstração pequena alinhada à vaga Java + Angular Sênior. Cada identificador abaixo corresponde a uma tarefa independente, executada e validada antes da próxima. A implementação da T01 foi autorizada após a entrega do planejamento.

Convenção proposta: branch `feat/t01-fundacao` e equivalentes; commits focados; PR com problema, mudança e validação. Não marcar tarefas como concluídas sem conferir os critérios de aceite. T00 e T01 estão concluídas; T02 é a próxima tarefa. Git, remoto e PRs ainda não foram configurados nestes diretórios.

## Regras iniciais a implementar

- Frete possui veículo, motorista, origem, destino, tipo de carga, prazo em UTC e custo contratado em BRL. O MVP usa uma única moeda.
- Estados: PLANEJADO → EM_TRANSITO → ENTREGUE; cancelamento permitido apenas em PLANEJADO.
- Um veículo não pode ter dois fretes EM_TRANSITO simultaneamente.
- Atraso ocorre quando o instante atual ultrapassa o prazo de um frete EM_TRANSITO. Gerar apenas um incidente de atraso por frete; reconhecer o incidente não altera o prazo nem entrega o frete.
- Posição contém identificador do evento, veículo, coordenadas e instante UTC. Coordenadas inválidas são rejeitadas; eventos repetidos não duplicam efeitos; posição antiga não substitui a mais recente.
- MOTORISTA consulta apenas seus fretes e posições; OPERADOR gerencia a operação; DIRETOR acessa somente indicadores agregados. A API aplica as restrições, além dos guards do Angular.
- Dados e atores de negócio são fictícios. Hipóteses de negócio serão registradas como hipóteses, sem atribuir entrevistas que não ocorreram.

## MVP — prioridade de entrega

### T00 — Planejamento e apresentação

Entrega: README comercial e técnico, recorte do MVP e sequência de tarefas.

Aceite: requisitos reais da vaga mapeados; funcionalidades planejadas claramente identificadas; nenhum código de aplicação iniciado.

### T01 — Fundação e ambiente local

Status: concluída. Evidências em [T01-VALIDACAO.md](T01-VALIDACAO.md).

Depende de T00. Criar projetos Spring Boot e Angular; fixar versões compatíveis, wrappers e lockfile; preparar PostgreSQL em Docker Compose, configuração de exemplo, migração inicial e endpoint de saúde. Definir contrato REST inicial e ADR do monólito modular.

Aceite: ambos os projetos compilam; banco e API iniciam seguindo instruções verificadas; Angular abre; configuração não contém credenciais reais. README recebe comandos de execução reais.

### T02 — Domínio de fretes e API REST

Depende de T01. Implementar veículo, motorista, frete, transições e restrição de veículo em uso; persistência com migrações, DTOs, validação, paginação, filtros e erros consistentes. Documentar endpoints no OpenAPI.

Aceite: cadastrar e listar fretes funciona com PostgreSQL; transições inválidas e uso simultâneo do veículo são recusados; testes cobrem regras e persistência, incluindo concorrência na alocação.

### T03 — Segurança e acesso por recurso

Depende de T02. Implementar autenticação local com Spring Security e os três perfis; armazenar senhas com hash e prover usuários fictícios de demonstração. Documentar sessão, expiração e configuração de CORS/CSRF conforme a estratégia escolhida em ADR.

Aceite: acesso anônimo recebe 401; ação proibida recebe 403; motorista não acessa frete alheio mesmo conhecendo o ID; diretor não executa operações de escrita. Testar a matriz de permissões na API.

### T04 — SOAP/XML e evolução de legado

Depende de T03. Criar transportadora SOAP simulada com WSDL versionado para cotação de frete; integrar por adaptador com timeout e tratamento de SOAP Fault. Manter uma implementação didática isolada de regra de cotação antiga, caracterizá-la com testes e documentar sua refatoração preservando comportamento.

Aceite: cotação válida pode ser consultada e seu valor registrado no frete; timeout, XML inválido e SOAP Fault produzem erro controlado; testes demonstram equivalência da regra antes/depois. O README identifica explicitamente que o legado é uma simulação didática.

### T05 — Angular: operação de fretes

Depende de T04. Criar login, navegação por perfil, interceptor, guards, listagem paginada, filtros e formulário de frete com Material. Usar Signals para estado da tela e RxJS para chamadas e cancelamento de buscas.

Aceite: fluxo de login → cadastro → consulta SOAP → listagem funciona pela UI; filtros e validações são demonstráveis; carregamento, vazio, falha e sessão expirada têm tratamento; restrições reais permanecem na API.

### T06 — Rastreamento e incidentes

Depende de T05. Criar simulador determinístico de GPS, ingestão REST, última posição persistida e mapa Leaflet com atualização periódica. Implementar avaliação periódica de atraso e reconhecimento de incidentes. Separar o relógio da regra para testes reproduzíveis.

Aceite: simulação movimenta veículo no mapa; repetição e reordenação de eventos não corrompem a posição; frete em trânsito vencido gera um único incidente, inclusive sem novos pontos GPS; operador reconhece o incidente. Testes cobrem limites temporais e autorização.

### T07 — MVP demonstrável e evidências

Depende de T06. Adicionar indicadores simples de fretes, atrasos e custos contratados, dados sintéticos reproduzíveis, CI nos dois projetos e roteiro de demonstração de cinco minutos. Registrar decisões de uso de IA, correções humanas e comandos de validação efetivamente executados.

Aceite: ambiente limpo executa o roteiro documentado; builds e testes relevantes passam; um teste ponta a ponta cobre o fluxo principal; screenshots reais e limitações conhecidas constam nos READMEs. Esta é a primeira versão para apresentar na candidatura.

## Evoluções após o MVP

### T08 — Atualização em tempo real

Depende de T07. Substituir atualização periódica por WebSocket autenticado; usar RxJS para limitar renderizações e reconectar, reconciliando o estado com um snapshot da API.

Aceite: atualização sem recarregar; reconexão recupera o estado; motorista não recebe eventos de terceiros; destruição da tela encerra subscriptions. Medir comportamento com volume sintético definido antes de publicar números.

### T09 — Mensageria e cache

Depende de T08. Introduzir RabbitMQ na ingestão, com confirmação de publicação, reconhecimento após processamento, tentativas limitadas, fila de mensagens mortas e idempotência persistida. Usar Redis para última posição com TTL e fallback para PostgreSQL; documentar consistência e recuperação.

Aceite: reinício do consumidor e entrega duplicada não duplicam incidentes; mensagem inválida chega à fila de mensagens mortas; queda do Redis permite leitura via banco; posição antiga não sobrescreve a atual. Atualizar Compose e diagrama com o fluxo implementado.

### T10 — Extração de serviços e detecção de desvio

Depende de T09. Avaliar em ADR o benefício de extrair telemetria e alertas. Se justificado, definir contratos, ownership de dados e propagação de falhas antes da extração. Adicionar rota planejada e regra de desvio com tolerância geográfica explícita.

Aceite: limites e custos operacionais documentados; testes de contrato e falha entre serviços; desvio comprovado com trajetos sintéticos dentro e fora da tolerância. Executar somente se houver tempo e valor para a demonstração.

## Critério comum de conclusão

Em cada tarefa: implementar apenas o escopo previsto, executar as verificações relevantes, atualizar documentação afetada e relatar resultado e limitações. Registrar sugestões de IA que exigiram correção e a evidência usada para validá-las. Não afirmar escala, experiência de produção ou otimização de custos sem evidência.

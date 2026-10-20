# ADR 0001 — Monólito modular para o MVP

Data: 20/10/2026. Status: aceito para a fundação.

## Contexto

O projeto precisa entregar rapidamente um fluxo demonstrável de Java, Angular,
REST e integração SOAP. Ainda não há volume real nem necessidade comprovada de
deploys independentes.

## Decisão

Usar uma aplicação Spring Boot, organizada por capacidade de negócio quando os
casos de uso forem implementados: fretes, telemetria, incidentes e integrações.
Cada módulo terá domínio, aplicação e adaptadores conforme necessário. Não criar
interfaces e camadas sem responsabilidade concreta.

Spring MVC expõe HTTP; PostgreSQL persiste dados e Flyway controla mudanças de
schema. A T01 cria somente o schema `logitrack`; tabelas ficam para a T02. A
primeira migração é executada automaticamente na inicialização.

O frontend Angular standalone usa configuração estrita e proxy local `/api`
para o backend. O proxy evita configurar CORS antes da definição de autenticação.
Backend e PostgreSQL escutam apenas em loopback no ambiente local.

Java 21 é a versão alvo; Maven Wrapper fixa a ferramenta de build. Spring Boot
gerencia as versões das dependências Java; o frontend fixa versões diretas e
mantém `package-lock.json`. Node é fixado em `.nvmrc`. Atualizações devem passar
pelas mesmas verificações da fundação.

## Consequências

Uma única transação e um único deploy simplificam o MVP. Os módulos compartilharão
o banco inicialmente; isso exige disciplina para evitar acesso cruzado a tabelas.
A extração de serviços exigirá rever contratos, consistência, observabilidade e
ownership dos dados na T10.

A autenticação será implementada na T03. A T01 só expõe saúde, sem detalhes internos
ou dados de negócio. Não há autorização de produção implícita no ambiente local.

## Alternativas consideradas

Microsserviços desde o início introduziriam rede, broker, coordenação de falhas e
mais ambientes antes da validação do fluxo. A separação fica prevista após o MVP,
com uma justificativa mensurável antes da extração.

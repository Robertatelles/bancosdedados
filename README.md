**BancosDeDados**

Projeto demonstrativo que integra múltiplos sistemas gerenciadores de banco de dados (PostgreSQL, Redis, MongoDB e Neo4j), implementando operações CRUD para a entidade Pessoa em Java.

**Visão Geral**

Este repositório tem como objetivo apresentar a integração prática de diferentes tecnologias de banco de dados dentro de uma única aplicação Java, facilitando o entendimento sobre uso combinado de bancos relacionais, NoSQL, cache e grafos em projetos reais.

**Tecnologias Utilizadas**

- Java 22
- PostgreSQL: banco relacional utilizado para armazenamento primário dos dados da entidade Pessoa.
- Redis: solução de cache para otimização de consultas por CPF.
- MongoDB: banco NoSQL para registro de logs e auditoria das operações realizadas.
- Neo4j: banco de grafos para modelagem e consulta de relacionamentos entre pessoas.

**Estrutura do Projeto**

- model: definição da classe Pessoa e seus atributos.
- repository: implementação dos acessos do Postgres, MongoDB e Neo4j.
- database: configurações e conexões com os bancos de dados utilizados.
- menu: interface de linha de comando para o usuário interagir com o sistema.
- Main: classe principal que exemplifica a utilização dos componentes do sistema.

**Funcionalidades**

- Implementação completa do ciclo CRUD para a entidade Pessoa (campos: id, nome, email, cpf, dataNascimento, amizade).
- Registro detalhado de logs de operações no MongoDB, garantindo rastreabilidade e auditoria.
- Cache em Redis para acelerar buscas frequentes por CPF, reduzindo a latência de consultas.
- Gerenciamento e consulta de relacionamentos de amizade entre pessoas utilizando Neo4j.

**Como Executar**

1. Prepare os ambientes dos bancos de dados PostgreSQL, Redis, MongoDB e Neo4j. Ajuste as configurações de conexão nos arquivos correspondentes do pacote database.

2. Clone o repositório:

   git clone https://github.com/Robertatelles/bancosdedados.git
   cd bancosdedados

3. Execute a aplicação.

4. Utilize o console para interagir com o sistema e testar as funcionalidades implementadas.
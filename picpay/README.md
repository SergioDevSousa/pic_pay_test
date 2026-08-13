# PicPay API

API REST desenvolvida em **Java com Spring Boot** para simular um fluxo simplificado de transferências financeiras entre usuários comuns e lojistas.

O projeto foi desenvolvido com foco nas regras de negócio propostas pelo desafio **PicPay Simplificado**, incluindo controle de saldo, restrição de transferências por tipo de usuário, autorização externa, persistência transacional e notificação do recebedor.

---

## Sobre o projeto

A aplicação representa uma carteira digital simplificada com dois tipos de usuários:

* **COMMON** — usuário comum
* **MERCHANT** — lojista

Usuários comuns podem enviar e receber dinheiro.

Lojistas podem receber transferências, mas **não podem realizar transferências**.

O principal fluxo da aplicação é:

```text
Pagador
   ↓
Validação dos usuários
   ↓
Validação do tipo do pagador
   ↓
Validação do saldo
   ↓
Serviço autorizador externo
   ↓
Débito do pagador
   ↓
Crédito do recebedor
   ↓
Persistência da transferência
   ↓
Notificação do recebedor
```

---

## Tecnologias

O projeto utiliza:

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Bean Validation
* Spring Security Crypto / BCrypt
* H2 Database
* Maven
* API REST
* RestClient
* Jakarta Persistence
* Jakarta Validation

---

## Arquitetura

A aplicação utiliza uma arquitetura em camadas:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Além das camadas principais, DTOs e Mappers são utilizados para evitar o acoplamento direto entre as entidades persistidas e os contratos HTTP.

```text
Request JSON
     ↓
Controller
     ↓
Request DTO
     ↓
Service
     ↓
Entity
     ↓
Repository
     ↓
H2
```

Na resposta:

```text
Entity
   ↓
Mapper
   ↓
Response DTO
   ↓
Controller
   ↓
JSON
```

---

## Estrutura do projeto

```text
src/main/java/br/com/picpay
│
├── PicpayApplication.java
│
├── controller
│   ├── UserController.java
│   └── TransferController.java
│
├── service
│   ├── UserService.java
│   ├── TransferService.java
│   ├── AuthorizationService.java
│   └── NotificationService.java
│
├── repository
│   ├── UserRepository.java
│   └── TransactionRepository.java
│
├── entity
│   ├── User.java
│   └── Transaction.java
│
├── dto
│   ├── request
│   │   ├── UserRequest.java
│   │   └── TransferRequest.java
│   │
│   └── response
│       ├── UserResponse.java
│       ├── TransferResponse.java
│       ├── AuthorizationResponseDto.java
│       └── AuthorizationDataDto.java
│
├── mapper
│   └── UserMapper.java
│
├── enums
│   └── UserType.java
│
└── exception
    ├── BusinessException.java
    ├── ResourceNotFoundException.java
    ├── GlobalExceptionHandler.java
    └── ErrorResponse.java
```

---

## Modelo de domínio

### User

Representa os usuários da carteira.

Principais atributos:

```text
id
fullName
document
email
password
balance
userType
```

O campo `userType` determina as permissões financeiras do usuário.

### UserType

```java
COMMON
MERCHANT
```

| Tipo       | Enviar dinheiro | Receber dinheiro |
| ---------- | --------------- | ---------------- |
| `COMMON`   | Sim             | Sim              |
| `MERCHANT` | Não             | Sim              |

### Transaction

Representa uma transferência realizada entre dois usuários.

Principais informações:

```text
id
value
payer
payee
createdAt
```

Onde:

* `payer` é o usuário que envia o dinheiro;
* `payee` é o usuário que recebe o dinheiro;
* `value` representa o valor da transferência.

---

## Regras de negócio

A aplicação implementa as principais regras solicitadas pelo desafio.

### Documento único

CPF/CNPJ não pode ser utilizado por mais de um usuário.

A aplicação realiza validação através do repository e também utiliza restrição de unicidade no banco de dados.

### E-mail único

Não é permitido cadastrar dois usuários utilizando o mesmo endereço de e-mail.

### Senha

As senhas não são persistidas em texto puro.

Antes da persistência, a senha é processada utilizando **BCrypt**.

### Transferências entre usuários

Um usuário `COMMON` pode transferir dinheiro para:

```text
COMMON → COMMON
COMMON → MERCHANT
```

### Restrição para lojistas

Um usuário `MERCHANT` não pode iniciar uma transferência:

```text
MERCHANT → COMMON    ❌
MERCHANT → MERCHANT  ❌
```

O lojista pode apenas receber pagamentos.

### Validação de saldo

Antes de realizar uma transferência, a aplicação verifica se o pagador possui saldo suficiente.

Exemplo:

```text
Saldo disponível: R$ 500,00
Transferência:    R$ 100,00

Resultado:

Saldo pagador:    R$ 400,00
```

Uma tentativa de transferência superior ao saldo disponível é rejeitada.

---

## Autorização externa

Antes de efetivar uma transferência, a aplicação consulta um serviço externo de autorização.

Método utilizado:

```http
GET /api/v2/authorize
```

Serviço utilizado no desafio:

```text
https://util.devi.tools/api/v2/authorize
```

O fluxo é:

```text
Solicitação de transferência
          ↓
Validações internas
          ↓
Consulta ao autorizador
          ↓
     Autorizado?
       /     \
     Sim     Não
      ↓       ↓
Transfere   Cancela
```

Caso o serviço não autorize a operação, nenhuma movimentação financeira deve ser concluída.

---

## Transação financeira

A operação financeira utiliza controle transacional do Spring através de:

```java
@Transactional
```

A transferência envolve pelo menos três operações:

```text
1. Debitar saldo do pagador
2. Creditar saldo do recebedor
3. Registrar a transferência
```

Essas operações devem ser tratadas como uma única unidade.

Caso uma inconsistência provoque uma exceção durante a operação:

```text
BEGIN TRANSACTION
       ↓
debita payer
       ↓
credita payee
       ↓
ocorre erro
       ↓
ROLLBACK
```

O banco retorna ao estado anterior.

Isso evita situações em que o dinheiro seja debitado de um usuário sem ser creditado ao destinatário.

---

## Notificação

Após uma transferência, o recebedor deve ser notificado.

A aplicação integra-se ao serviço mock disponibilizado pelo desafio:

```http
POST /api/v1/notify
```

Serviço:

```text
https://util.devi.tools/api/v1/notify
```

A notificação é tratada separadamente da regra financeira, considerando que serviços externos podem apresentar indisponibilidade ou instabilidade.

Em uma aplicação de produção, esse processo poderia evoluir para uma solução assíncrona utilizando mensageria, retry e padrões como Transactional Outbox.

---

# Endpoints

## Cadastrar usuário

```http
POST /users
Content-Type: application/json
```

### Usuário comum

```json
{
  "fullName": "Sergio Sousa",
  "document": "12345678901",
  "email": "sergio@email.com",
  "password": "123456",
  "balance": 1000.00,
  "userType": "COMMON"
}
```

### Lojista

```json
{
  "fullName": "Loja Central",
  "document": "12345678000199",
  "email": "contato@lojacentral.com",
  "password": "123456",
  "balance": 0.00,
  "userType": "MERCHANT"
}
```

Resposta esperada:

```http
201 Created
```

---

## Listar usuários

```http
GET /users
```

Resposta:

```http
200 OK
```

Exemplo:

```json
[
  {
    "id": 1,
    "fullName": "Sergio Sousa",
    "document": "12345678901",
    "email": "sergio@email.com",
    "balance": 1000.00,
    "userType": "COMMON"
  },
  {
    "id": 2,
    "fullName": "Loja Central",
    "document": "12345678000199",
    "email": "contato@lojacentral.com",
    "balance": 0.00,
    "userType": "MERCHANT"
  }
]
```

---

## Buscar usuário por ID

```http
GET /users/{id}
```

Exemplo:

```http
GET /users/1
```

Resposta:

```http
200 OK
```

Caso o usuário não exista:

```http
404 Not Found
```

---

# Realizar transferência

Este é o principal endpoint da aplicação e segue o contrato solicitado pelo desafio.

```http
POST /transfer
Content-Type: application/json
```

Request:

```json
{
  "value": 100.0,
  "payer": 1,
  "payee": 2
}
```

Onde:

```text
value  → valor da transferência
payer  → ID do pagador
payee  → ID do recebedor
```

O `payer` precisa ser um usuário `COMMON`.

O `payee` pode ser:

```text
COMMON
ou
MERCHANT
```

---

## Fluxo da transferência

```text
POST /transfer
       │
       ▼
Localizar payer
       │
       ▼
Localizar payee
       │
       ▼
Validar valor
       │
       ▼
Payer é COMMON?
       │
       ▼
Possui saldo?
       │
       ▼
Consultar autorizador externo
       │
       ▼
Foi autorizado?
       │
       ▼
Debitar payer
       │
       ▼
Creditar payee
       │
       ▼
Registrar Transaction
       │
       ▼
COMMIT
       │
       ▼
Notificar payee
```

---

## Exemplo de transferência

Estado inicial:

```text
ID 1
Sergio
COMMON
Saldo: R$ 1.000,00

ID 2
Loja Central
MERCHANT
Saldo: R$ 0,00
```

Request:

```json
{
  "value": 100.0,
  "payer": 1,
  "payee": 2
}
```

Após a transferência:

```text
Sergio
R$ 1.000,00 → R$ 900,00

Loja Central
R$ 0,00 → R$ 100,00
```

---

# Tratamento de erros

A aplicação possui tratamento centralizado de exceções.

Entre os cenários tratados estão:

* usuário não encontrado;
* documento duplicado;
* e-mail duplicado;
* dados inválidos;
* saldo insuficiente;
* tentativa de transferência realizada por lojista;
* transferência não autorizada;
* valor de transferência inválido.

Exemplo:

```json
{
  "status": 400,
  "message": "Saldo insuficiente",
  "timestamp": "2026-08-08T18:41:56"
}
```

Para erros de validação:

```json
{
  "status": 400,
  "message": "Dados inválidos",
  "errors": {
    "password": "não deve estar em branco"
  }
}
```

---

# Banco de dados

Durante o desenvolvimento foi utilizado o **H2 Database**, permitindo executar e testar a aplicação sem necessidade de infraestrutura externa.

Exemplo de configuração:

```properties
spring.datasource.url=jdbc:h2:mem:picpaydb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update

spring.h2.console.enabled=true
```

> Quando configurado com `jdbc:h2:mem:picpaydb`, os dados permanecem apenas enquanto a aplicação estiver em execução.

---

# Executando o projeto

## Pré-requisitos

É necessário possuir:

```text
Java 21+
Maven ou Maven Wrapper
```

Verifique o Java:

```bash
java -version
```

---

## Clonar o projeto

```bash
git clone <URL_DO_REPOSITORIO>
```

Entre no diretório:

```bash
cd picpay
```

---

## Executar com Maven Wrapper

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Também é possível utilizar Maven instalado localmente:

```bash
mvn spring-boot:run
```

Após a inicialização:

```text
http://localhost:8080
```

---

# Testes manuais

Os endpoints podem ser testados utilizando Postman, Insomnia ou qualquer cliente HTTP.

Uma sequência básica de testes é:

```text
1. POST /users
      ↓
Cadastrar usuário COMMON

2. POST /users
      ↓
Cadastrar usuário MERCHANT

3. GET /users
      ↓
Confirmar os IDs

4. POST /transfer
      ↓
Transferir COMMON → MERCHANT

5. GET /users
      ↓
Conferir alteração dos saldos
```

Também devem ser testados cenários de erro:

```text
COMMON sem saldo → transferência
MERCHANT → COMMON
documento duplicado
e-mail duplicado
payer inexistente
payee inexistente
valor inválido
transferência não autorizada
```

---

# Decisões técnicas

## Entidade única para usuários

Usuários comuns e lojistas compartilham a mesma entidade `User`.

A diferenciação é realizada através de:

```java
UserType.COMMON
UserType.MERCHANT
```

Essa decisão simplifica o relacionamento da transferência, pois tanto `payer` quanto `payee` referenciam usuários da mesma tabela.

---

## BigDecimal para valores monetários

Valores financeiros são representados com:

```java
BigDecimal
```

em vez de `double` ou `float`.

Isso evita problemas de precisão inerentes a tipos de ponto flutuante.

---

## DTOs

As entidades JPA não são utilizadas diretamente como contrato da API.

A aplicação utiliza:

```text
Request DTO
Response DTO
```

Isso permite controlar os dados recebidos e expostos pela API.

Um exemplo importante é a senha: ela é recebida durante o cadastro, mas não precisa ser devolvida no `UserResponse`.

---

## Mapper

A camada de mapper é responsável pela transformação:

```text
DTO ↔ Entity
```

evitando que essa responsabilidade fique concentrada nos controllers.

---

## BCrypt

As senhas são processadas utilizando:

```text
BCryptPasswordEncoder
```

evitando armazenamento de senha em texto puro.

---

## @Transactional

O fluxo financeiro utiliza transação de banco para preservar a consistência dos saldos.

A transferência deve obedecer ao princípio:


# Considerações técnicas

O projeto prioriza os requisitos centrais do desafio: modelagem dos usuários, validação das regras de transferência, integração com serviços externos e consistência da movimentação financeira.

A separação entre Controller, Service, Repository, DTO, Mapper e integrações externas permite que as responsabilidades permaneçam isoladas e facilita testes e futuras evoluções.

O ponto central da solução é garantir que uma transferência somente seja efetivada quando todas as condições financeiras necessárias forem satisfeitas e que uma falha durante a operação não deixe os saldos em estado inconsistente.

---

## Autor

**ALUNOS**

**Sergio Sousa**
**José Marcos**

**PROFESSOR**
**Carlos Barbosa**

Projeto desenvolvido na aula designer de arquitetura do curso MBA fullstack pela UNIESP. desafio técnico de backend utilizando Java e Spring Boot.

# Jogo da Forca — Multijogador

Jogo da Forca multijogador (2–4 jogadores) com arquitetura cliente-servidor sobre TCP.

## Compilar

```bash
javac -d out src/forca/*.java
```

## Executar

Num terminal, iniciar o servidor:

```bash
java -cp out:src/forca Server
```

Noutro(s) terminal(is), iniciar um cliente por jogador (mínimo 2):

```bash
java -cp out:src/forca Cliente
```

> **Nota:** São necessários pelo menos 2 clientes para o jogo começar. Após o 2.º jogador entrar, o lobby aguarda 20 segundos por mais jogadores (máx. 4).

## Regras

- Cada ronda, todos os jogadores submetem uma letra ou a palavra completa.
- Tempo limite por ronda: 20 segundos.
- 6 tentativas no total: cada jogada errada consome uma.
- Viória quando todas as letras são reveladas; derrota quando as tentativas chegam a 0.

## Protocolo

| Direção | Mensagem                               | Descrição                |
|---------|----------------------------------------|--------------------------|
| S→C     | `WELCOME <id> <total>`                 | Identificação do jogador |
| S→C     | `START <mask> <attempts> <timeout>`    | Início do jogo           |
| S→C     | `ROUND <n> <mask> <attempts> <letras>` | Nova ronda               |
| S→C     | `END WIN <ids> <palavra>`              | Vitória                  |
| S→C     | `END LOSE <palavra>`                   | Derrota                  |
| C→S     | `GUESS <tentativa>`                    | Jogada do cliente        |



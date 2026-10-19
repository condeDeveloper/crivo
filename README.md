# Crivo de Eratóstenes

O crivo de Eratóstenes em Java 21, em quatro versões, com o trabalho de cada uma contado em duas colunas separadas.

O crivo é o exemplo clássico de algoritmo onde "otimizações óbvias" valem coisas muito diferentes, e quase ninguém mede qual vale o quê. Uma das três melhorias aqui corta 46% do trabalho. Outra corta 39%. A terceira, que é a mais complicada de escrever, corta **0,004%**.

```
mvn -q exec:java -Dexec.args=roda
```

## Risco e visita

A separação entre as duas é o que faz este repositório ter algo a dizer.

**Risco** é marcar uma posição como composta. **Visita** é olhar um candidato para decidir se vale a pena riscar os múltiplos dele.

Quase toda discussão sobre otimizar crivo mistura as duas, e uma das versões aqui melhora só a segunda, que já era quase nada.

## As medidas

### 1. As quatro versões contra a divisão por tentativa

| até | primos | divergências |
|---:|---:|---:|
| 100 | 25 | 0 |
| 1.000 | 168 | 0 |
| 10.000 | 1.229 | 0 |
| 100.000 | 9.592 | 0 |

As quatro riscam múltiplos; a divisão por tentativa pergunta, para cada número, se algum candidato até a raiz o divide. São ideias diferentes, e por isso a divisão pode julgar: ela não erra do mesmo jeito que um crivo com índice trocado erraria.

### 2. Quanto cada versão trabalha

Até dez milhões:

| versão | riscos | visitas | total |
|---|---:|---:|---:|
| ingênuo | 29.465.738 | 9.999.999 | 39.465.737 |
| do quadrado | 22.850.051 | **3.161** | 22.853.212 |
| sem pares | 13.925.143 | 1.580 | 13.926.723 |
| roda de 2 e 3 | **13.925.143** | 1.053 | 13.926.196 |

Começar no quadrado mexe nas **duas** colunas: risca 22% a menos, e, por parar o laço de fora na raiz, derruba as visitas de dez milhões para 3.161.

### 3. O que cada otimização vale de verdade

Até dez milhões:

| de | para | cortou dos riscos | cortou do total |
|---|---|---:|---:|
| ingênuo | do quadrado | 22,45% | **42,09%** |
| do quadrado | sem pares | 39,06% | **39,06%** |
| sem pares | roda de 2 e 3 | **0,00%** | **0,00%** |

A roda de 2 e 3 risca **exatamente a mesma coisa** que pular pares: 13.925.143 riscos nas duas.

A diferença inteira entre elas são **527 visitas** em 13,9 milhões de operações, ou 0,0038%. Ela é mais complicada de escrever, mais fácil de errar, e não risca um número a menos. O ganho dela está numa coluna que já era desprezível.

O motivo é simples quando se olha: pular os pares já trata o 3 normalmente, riscando 9, 15, 21 e assim por diante. A roda apenas deixa de **examinar** os múltiplos de 3 como candidatos, e examinar candidatos nunca foi o custo.

### 4. O crivo contra a divisão por tentativa

| até | crivo | divisão | quantas vezes |
|---:|---:|---:|---:|
| 10.000 | 11.026 | 55.958 | 5x |
| 100.000 | 121.659 | 1.345.442 | 11x |
| 1.000.000 | 1.311.399 | 33.600.560 | **26x** |

A margem **cresce**. Não é uma constante melhor, é outra ordem de trabalho.

Mas vale a honestidade: as duas respondem perguntas diferentes. A divisão por tentativa diz se **um** número é primo sem precisar de memória nenhuma, e para essa pergunta o crivo não serve.

### 5. Quantos primos existem, e o que a estimativa erra

| até | primos | n/ln(n) | erro |
|---:|---:|---:|---:|
| 100 | 25 | 21,7 | -13,1% |
| 1.000 | 168 | 144,8 | -13,8% |
| 10.000 | 1.229 | 1.085,7 | -11,7% |
| 100.000 | 9.592 | 8.685,9 | -9,4% |
| 1.000.000 | 78.498 | 72.382,4 | **-7,8%** |

A primeira aproximação do teorema dos números primos **subestima sempre**, e o erro cai devagar: ainda são 7,8% com um milhão, bem mais do que a fama da fórmula sugere.

E o que limita o crivo na prática não é o tempo: é o vetor de marcas, que gasta um byte por número. Dez milhões custam dez megabytes antes de qualquer conta.

## O que as medidas me corrigiram

**A otimização mais trabalhosa é a que não vale nada.** A roda de 2 e 3 tem fama de ser o próximo passo natural depois de pular os pares. Ela risca o mesmo número exato de posições. Escrevi as duas esperando ver um terceiro corte e vi zero.

**Começar no quadrado é duas mudanças disfarçadas de uma.** Ela corta 22% dos riscos, o que é o que se costuma dizer dela. O corte grande está em outro lugar: parar o laço de fora na raiz, que tira 10 milhões de visitas. Juntas dão 42%, e quem atribui tudo ao quadrado está olhando a metade menor.

**"O crivo é mais rápido" precisa de qualificação.** Ele é 26 vezes mais barato para listar todos os primos até um milhão, e é inútil para dizer se um número de vinte dígitos é primo. São perguntas diferentes, e a comparação só vale para uma delas.

## Rodar

```bash
mvn -q verify                              # 17 testes
mvn -q exec:java                           # todas as medidas
mvn -q exec:java -Dexec.args=correcao      # 1
mvn -q exec:java -Dexec.args=quadrado      # 2
mvn -q exec:java -Dexec.args=roda          # 3
mvn -q exec:java -Dexec.args=divisao       # 4
mvn -q exec:java -Dexec.args=memoria       # 5
```

Java 21, sem dependência fora do JUnit. Nenhum número deste README foi digitado à mão: todos saem de `Medidas`, e os testes conferem os que aparecem escritos aqui.

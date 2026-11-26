package com.example.accurancymobileapp.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Perguntas frequentes do app. O texto segue a página de ajuda do site,
 * trocando os links das páginas pelas abas do aplicativo.
 */
public final class ConteudoAjuda {

    public static final class Pergunta {
        public final String pergunta;
        public final String resposta;

        Pergunta(String pergunta, String resposta) {
            this.pergunta = pergunta;
            this.resposta = resposta;
        }
    }

    public static final class Secao {
        public final String titulo;
        public final List<Pergunta> perguntas;

        Secao(String titulo, Pergunta... perguntas) {
            this.titulo = titulo;
            this.perguntas = Arrays.asList(perguntas);
        }
    }

    private ConteudoAjuda() {}

    public static List<Secao> secoes() {
        return Arrays.asList(
                new Secao("Conta e cadastro",
                        new Pergunta("Preciso informar CPF para criar a conta?",
                                "Não. O cadastro pede só nome, e-mail e senha. O telefone é opcional "
                                        + "e pode ser adicionado depois, na aba Perfil."),
                        new Pergunta("Não recebi o código de confirmação do e-mail",
                                "Confira a caixa de spam e a aba de promoções. O código vale por 10 minutos; "
                                        + "se ele expirar, peça um novo na tela de confirmação. Você só consegue "
                                        + "entrar depois de confirmar o e-mail."),
                        new Pergunta("Como altero meu nome, telefone, e-mail ou senha?",
                                "Na aba Perfil, toque no dado que quer mudar. Nome e telefone mudam na hora. "
                                        + "Para trocar o e-mail ou a senha, enviamos um código de confirmação "
                                        + "para o seu e-mail atual, por segurança."),
                        new Pergunta("O app e o site usam a mesma conta?",
                                "Sim. As carteiras, o saldo, o histórico e as notificações são os mesmos nos "
                                        + "dois. O que você faz em um aparece no outro.")),

                new Secao("Carteiras",
                        new Pergunta("Qual a diferença entre carteira real e simulada?",
                                "A carteira real usa o saldo geral da conta e entra no seu patrimônio total. "
                                        + "A carteira simulada tem um dinheiro próprio, que você escolhe ao criá-la: "
                                        + "serve para testar estratégias sem mexer no saldo geral, e o resultado "
                                        + "dela aparece separado.\n\nNos dois casos o dinheiro é fictício: o "
                                        + "Accuracy é uma plataforma de estudo."),
                        new Pergunta("Como coloco mais dinheiro em uma simulação?",
                                "Abra a carteira simulada na aba Carteira e toque em Depositar na simulação. "
                                        + "O valor entra só naquela carteira e conta como capital aplicado no "
                                        + "cálculo do resultado da simulação."),
                        new Pergunta("O que é o estilo da carteira?",
                                "É o foco que você escolhe para a carteira: renda fixa, renda variável, "
                                        + "cripto, internacional ou mista. Ele serve para organizar e identificar "
                                        + "suas carteiras; qualquer ativo pode ser registrado em qualquer uma delas."),
                        new Pergunta("Por que não consigo excluir minha carteira real?",
                                "Uma carteira real só pode ser excluída quando não tem ativos. Venda os ativos "
                                        + "primeiro: o dinheiro da venda volta para o saldo geral e nada some do "
                                        + "seu patrimônio. Carteiras simuladas podem ser excluídas a qualquer momento."),
                        new Pergunta("Para que serve a meta da carteira?",
                                "É o valor de patrimônio que você quer alcançar naquela carteira. A barra de "
                                        + "progresso mostra quanto falta, e você recebe uma notificação quando a "
                                        + "meta é atingida.")),

                new Secao("Investimentos e operações",
                        new Pergunta("Como compro um ativo?",
                                "Na aba Aportes, busque o ativo pelo código ou pelo nome (por exemplo PETR4, "
                                        + "MXRF11 ou BTC), abra os detalhes e toque em Comprar. Depois escolha a "
                                        + "quantidade e a carteira que vai receber o investimento."),
                        new Pergunta("Como vendo um ativo?",
                                "Abra a carteira na aba Carteira e toque em Vender no ativo, ou use o botão "
                                        + "Vender na tela de detalhes do ativo. O valor da venda volta para o saldo "
                                        + "de onde o dinheiro saiu: o saldo geral, numa carteira real, ou o saldo "
                                        + "da simulação."),
                        new Pergunta("Por que minha compra foi recusada?",
                                "Os motivos mais comuns são: saldo insuficiente (o saldo geral, numa "
                                        + "carteira real, ou o saldo da simulação); o preço ficou muito diferente "
                                        + "da cotação atual (atualize a tela e tente de novo); ou o código não "
                                        + "existe no mercado."),
                        new Pergunta("De onde vêm as cotações?",
                                "As cotações de ações, FIIs, BDRs, ETFs, moedas e criptomoedas vêm da brapi, "
                                        + "um serviço de dados do mercado brasileiro, e podem ter alguns minutos de "
                                        + "atraso. Sem cotação disponível, usamos o último fechamento registrado."),
                        new Pergunta("Por que a renda fixa aparece como valor estimado?",
                                "Títulos de renda fixa não têm cotação em bolsa. O valor atual é estimado a "
                                        + "partir da taxa contratada e dos indicadores do momento (Selic, CDI ou "
                                        + "IPCA), considerando os dias desde a aplicação."),
                        new Pergunta("Como funcionam os dividendos?",
                                "Dividendos só podem ser registrados para ativos que a carteira possui. O valor "
                                        + "entra no saldo (geral ou da simulação) e aparece no histórico e no "
                                        + "resultado da carteira.")),

                new Secao("Saldo e patrimônio",
                        new Pergunta("O que é o saldo disponível?",
                                "É o dinheiro fictício da sua conta que ainda não foi investido. Ele é usado "
                                        + "nas compras das carteiras reais. Para colocar mais, toque em Adicionar "
                                        + "saldo no Início."),
                        new Pergunta("Como o patrimônio total é calculado?",
                                "Patrimônio total = saldo disponível + valor atual dos ativos das carteiras "
                                        + "reais. As carteiras simuladas aparecem separadas, com o resultado de "
                                        + "cada simulação, para não misturar o dinheiro de teste com o da conta."),
                        new Pergunta("O que é preço médio?",
                                "É quanto você pagou, em média, por unidade de um ativo. Se comprou 10 cotas a "
                                        + "R$ 10 e depois 10 a R$ 20, o preço médio é R$ 15. Vender não muda o "
                                        + "preço médio das unidades que ficaram."),
                        new Pergunta("O que significam investido, resultado e rentabilidade?",
                                "Investido é o custo dos ativos que você ainda tem (quantidade × preço médio). "
                                        + "Resultado é a diferença entre o valor atual e o investido. "
                                        + "Rentabilidade é esse resultado em porcentagem.")),

                new Secao("Histórico",
                        new Pergunta("Onde vejo todas as minhas operações?",
                                "Na aba Histórico. Lá aparecem compras, vendas e dividendos, com o gráfico de "
                                        + "movimentações por mês."),
                        new Pergunta("Posso excluir uma operação registrada por engano?",
                                "Sim, pela página Histórico do site, no ícone de lixeira. O Accuracy desfaz o "
                                        + "efeito da operação: uma compra excluída devolve o dinheiro e uma venda "
                                        + "excluída tira o valor do saldo. Uma compra que já foi vendida só pode ser "
                                        + "excluída depois da venda.")),

                new Secao("Sobre a plataforma",
                        new Pergunta("O Accuracy é uma corretora?",
                                "Não. O Accuracy é um projeto educacional: nenhuma ordem é enviada à bolsa e "
                                        + "nenhum dinheiro real é movimentado. A ideia é aprender a investir "
                                        + "praticando com carteiras e acompanhando os resultados."),
                        new Pergunta("As informações do Accuracy são recomendação de investimento?",
                                "Não. Cotações, notícias, cursos e relatórios têm finalidade educacional e não "
                                        + "substituem a orientação de um profissional certificado."),
                        new Pergunta("Como funcionam as notificações?",
                                "O sino no Início e no Perfil mostra avisos sobre a sua conta: compras, vendas, "
                                        + "dividendos, depósitos, metas alcançadas e respostas da equipe. O número "
                                        + "indica quantos ainda não foram lidos."))
        );
    }
}

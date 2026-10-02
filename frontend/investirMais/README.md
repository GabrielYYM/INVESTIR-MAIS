# Carteira — INVESTIR MAIS

Front end da tela de Carteira, em React + Tailwind, consumindo a API Brapi para
as cotações e o back end Spring para os ativos cadastrados pelo usuário.

## Estrutura

```
src/
  components/
    Sidebar.jsx        menu lateral
    Topbar.jsx          barra superior (pesquisa, usuário)
    WalletCard.jsx       card com o valor total da carteira
    FilterTabs.jsx      abas "Todos / Ações internacionais / ..."
    AssetsTable.jsx      tabela "Lista de Ativos"
  hooks/
    useCarteira.js       junta ativos do backend + cotações da Brapi
  services/
    brapiService.js     chamada GET /quote/{tickers} na Brapi
    carteiraService.js   chamadas ao back end Spring (GET/POST ativos)
  pages/
    Carteira.jsx         monta a tela completa
```

## Como funciona o fluxo de dados

1. `carteiraService.getAtivosDoUsuario()` busca no back end Spring os ativos
   que o usuário cadastrou (ticker, tipo, quantidade, nota) — sem preço, pois
   isso é dado dele, não da Brapi.
2. `brapiService.getQuotes(tickers)` faz **uma única chamada** para a Brapi
   com todos os tickers separados por vírgula (`/quote/QQQ,FLRY3,WEGE3,MCHI`),
   em vez de uma requisição por ativo — importante porque a Brapi tem limite
   de requisições no plano gratuito.
3. `useCarteira.js` combina os dois: calcula `valorAtual = preço x quantidade`
   e o `percentual` de cada ativo sobre o total da carteira.
4. `Carteira.jsx` aplica os filtros de busca e de tipo de ativo sobre esse
   resultado já calculado.

## Rodando localmente

```bash
npm install axios lucide-react
cp .env.example .env
# edite o .env com seu token da Brapi (https://brapi.dev/dashboard)
npm run dev
```

## Próximos passos sugeridos

- Se o time seguir a ideia de cache no Redis discutida no grupo, o endpoint
  `GET /api/carteira/ativos` do back end pode já devolver o preço atual
  calculado a partir do cache, e o front deixa de chamar a Brapi diretamente
  — só troca a implementação de `useCarteira.js`, o restante da tela não muda.
- Implementar o modal do botão "Adicionar ativo", chamando
  `carteiraService.adicionarAtivo()`.
- Tratar o rate limit da Brapi (retry com backoff) em `brapiService.js`.

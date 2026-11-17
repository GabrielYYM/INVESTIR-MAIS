export const courseCategories = ["Investimentos", "Renda fixa", "Renda variável", "Fundos imobiliários", "Fundos de investimento", "Tesouro Direto", "Finanças pessoais", "Planejamento financeiro", "Economia", "Geral"];
export const categoryChoices = current => [...new Set([...courseCategories, ...(current ? [current] : [])])].map(name => ({ id: name, name }));

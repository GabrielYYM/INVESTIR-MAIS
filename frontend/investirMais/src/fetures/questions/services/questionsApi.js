const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

async function handleResponse(response) {
    if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error(error.message || 'Erro na requisição');
    }
    if (response.status === 204) return null;
    return response.json();
}

export const questionApi = {
    getQuestionsByCategory: async (categoryId) => {
        const response = await fetch(`${BASE_URL}/questions/categories/${categoryId}`);
        return handleResponse(response);
    },

    createQuestion: async (categoryId, questionData) => {
        const response = await fetch(`${BASE_URL}/questions/categories/${categoryId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(questionData),
        });
        return handleResponse(response);
    },

    updateQuestion: async (questionId, questionData) => {
        const response = await fetch(`${BASE_URL}/questions/${questionId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(questionData),
        });
        return handleResponse(response);
    },

    deleteQuestion: async (questionId) => {
        const response = await fetch(`${BASE_URL}/questions/${questionId}`, {
            method: 'DELETE',
        });
        return handleResponse(response);
    },

    evaluateAsset: async (assetId, evaluationsData) => {
        const response = await fetch(`${BASE_URL}/questions/assets/${assetId}/evaluations`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(evaluationsData),
        });
        return handleResponse(response);
    }
};
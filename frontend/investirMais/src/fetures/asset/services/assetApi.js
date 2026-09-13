const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

async function handleResponse(response) {
    if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error(error.message || 'Erro na requisição');
    }
    if (response.status === 204) return null;
    return response.json();
}

export const assetApi = {
    getAssetsByCategory: async (categoryId) => {
        const response = await fetch(`${BASE_URL}/assets/categories/${categoryId}/assets`);
        return handleResponse(response);
    },

    createAsset: async (categoryId, assetData) => {
        const response = await fetch(`${BASE_URL}/assets/categories/${categoryId}/assets`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(assetData),
        });
        return handleResponse(response);
    },

    updateAsset: async (assetId, assetData) => {
        const response = await fetch(`${BASE_URL}/assets/${assetId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(assetData),
        });
        return handleResponse(response);
    },

    deleteAsset: async (assetId) => {
        const response = await fetch(`${BASE_URL}/assets/${assetId}`, {
            method: 'DELETE',
        });
        return handleResponse(response);
    }
};
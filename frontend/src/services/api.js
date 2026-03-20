import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1/',
  headers: {
    'Content-Type': 'application/json'
  }
});

export const generateRoadmap = async (data) => {
  try {
    const response = await api.post('roadmap/generate', data);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const getRoles = async () => {
  try {
    const response = await api.get('roles');
    return response.data;
  } catch (error) {
    console.error('Error fetching roles:', error.response || error.message);
    throw error;
  }
};

export default api;

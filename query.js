const axios = require('axios');

const registerUsers = async () => {
  for (let i = 1; i <= 1000; i++) {
    const user = {
      email: `test${i}@example.com`,
      password: 'password123',
      firstName: 'John',
      lastName: `Doe ${i}`,
      initialBalance: 1000
    };

    try {
      const response = await axios.post('http://localhost:8081/api/users/register', user);
      console.log(`✅ Registered user ${i}: ${response.status}`);
    } catch (error) {
      console.error(`❌ Failed to register user ${i}: ${error.response?.status || error.message}`);
    }
  }
};

registerUsers();

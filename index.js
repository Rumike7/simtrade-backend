// Create separate SockJS sockets for each client
const socketPrices = new SockJS('http://localhost:8082/ws');
const socketHistorics = new SockJS('http://localhost:8082/ws');

// STOMP client for real-time prices
const client = new StompJs.Client({
    webSocketFactory: () => socketPrices,
    debug: (msg) => console.log('STOMP Debug:', msg),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000
});

// STOMP client for historical data
const client0 = new StompJs.Client({
    webSocketFactory: () => socketHistorics,
    debug: (msg) => console.log('STOMP Debug:', msg),
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000
});

// Subscribe to real-time prices
client.onConnect = (frame) => {
    console.log('Connected to WebSocket:', frame);
    client.subscribe('/topic/prices', (message) => {
        console.log('Received message:', message.body);
        const prices = JSON.parse(message.body);
        updatePriceList(prices);
    });
};

// Subscribe to historical data
client0.onConnect = (frame) => {
    console.log('Connected to WebSocket (historic):', frame);
    client0.subscribe('/topic/historics', (message) => {
        console.log('Received historic message:', message.body);
        // Optionally: handle historical data update here
    });
};

// Error handlers
client.onStompError = (frame) => {
    console.error('STOMP error (client):', frame);
};

client.onWebSocketError = (error) => {
    console.error('WebSocket error (client):', error);
};

client.onDisconnect = () => {
    console.log('Disconnected from WebSocket (client)');
};

// Activate clients
client.activate();
client0.activate();

// DOM update function
function updatePriceList(prices) {
    const list = document.getElementById('price-list');
    list.innerHTML = '';
    if (Array.isArray(prices)) {
        prices.forEach(stock => {
            const item = document.createElement('li');
            item.textContent = `${stock.symbol}: $${stock.price.toFixed(2)}`;
            list.appendChild(item);
        });
    } else if (prices?.symbol && prices?.price != null) {
        const item = document.createElement('li');
        item.textContent = `${prices.symbol}: $${prices.price.toFixed(2)}`;
        list.appendChild(item);
    }
}

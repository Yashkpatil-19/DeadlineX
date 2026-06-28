const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');
const { checkTaskRiskStatus } = require('./services/riskWatch');

const app = express();
app.use(cors());
app.use(express.json());

const server = http.createServer(app);
const io = socketIo(server, {
    cors: {
        origin: "*",
        methods: ["GET", "POST"]
    }
});

// Connected users mapping: userId -> socketId
const userSockets = new Map();

io.on('connection', (socket) => {
    console.log(`New connection registered: ${socket.id}`);

    // Register user session with userId
    socket.on('register_user', (userId) => {
        if (userId) {
            userSockets.set(userId.toString(), socket.id);
            console.log(`User ${userId} registered to socket ${socket.id}`);
        }
    });

    socket.on('disconnect', () => {
        for (const [userId, socketId] of userSockets.entries()) {
            if (socketId === socket.id) {
                userSockets.delete(userId);
                console.log(`User ${userId} disconnected`);
                break;
            }
        }
    });
});

// API endpoint for task updates which triggers risk analysis check
app.post('/api/tasks/risk-analyzer', (req, res) => {
    const { userId, task } = req.body;
    if (!task || !userId) {
        return res.status(400).json({ error: "Missing task or userId" });
    }

    // Trigger risk evaluation and emit socket event if criteria met
    const socketId = userSockets.get(userId.toString());
    if (socketId) {
        checkTaskRiskStatus(io, task, socketId);
    }

    res.json({ success: true, status: "Risk evaluation processed" });
});

const PORT = process.env.PORT || 5000;
server.listen(PORT, () => {
    console.log(`Real-Time Socket server listening on port ${PORT}`);
});

module.exports = { app, server, io, userSockets };

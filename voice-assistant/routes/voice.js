const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');
const { GoogleGenAI } = require('@google/genai');

// Task Schema
const TaskSchema = new mongoose.Schema({
    userId: { type: String, required: true },
    title: { type: String, required: true },
    description: { type: String, default: '' },
    deadline: { type: Date, required: true },
    daysRemaining: { type: Number, default: 0 },
    dailyTarget: { type: String, default: '' },
    difficulty: { type: Number, default: 3 },
    progress: { type: Number, default: 0, min: 0, max: 100 },
    subtasks: [{
        title: { type: String },
        status: { type: String, enum: ['PENDING', 'COMPLETED'], default: 'PENDING' }
    }],
    createdAt: { type: Date, default: Date.now }
});

const TaskModel = mongoose.models.Task || mongoose.model('Task', TaskSchema);

// POST /api/voice/command
router.post('/command', async (req, res) => {
    try {
        const { transcript, userId } = req.body;

        if (!transcript) {
            return res.status(400).json({ error: 'Transcript is required' });
        }
        if (!userId) {
            return res.status(400).json({ error: 'userId is required' });
        }

        // Initialize Gemini with @google/genai
        const apiKey = process.env.GEMINI_API_KEY;
        if (!apiKey) {
            return res.status(500).json({ error: 'GEMINI_API_KEY environment variable is not set' });
        }
        
        const ai = new GoogleGenAI({ apiKey });

        const systemInstruction = `You are the AI Voice Controller for Deadline Guardian AI.
Analyze the user's transcript and map it to a specific intent (ADD_TASK, UPDATE_PROGRESS, RESCHEDULE_TASK, or GENERAL_QUERY).
Provide a concise, speakable response (max 2 sentences) and extract the relevant fields for the action payload.

Examples:
- "Add a task to study for exams with 10 days remaining and a target of 2 chapters daily" -> ADD_TASK, title: "Study for Exams", daysRemaining: 10, dailyTarget: "2 chapters daily"
- "Update progress on task 65c2a123f by 20 percent" -> UPDATE_PROGRESS, taskId: "65c2a123f", progressDelta: 20
- "Reschedule my final exam prep task 65c2a123f to have 5 days remaining" -> RESCHEDULE_TASK, taskId: "65c2a123f", daysRemaining: 5
- "What is my schedule for today?" -> GENERAL_QUERY

Return strict JSON matching the requested schema.`;

        const response = await ai.models.generateContent({
            model: 'gemini-1.5-pro',
            contents: transcript,
            config: {
                systemInstruction,
                responseMimeType: 'application/json',
                responseSchema: {
                    type: 'OBJECT',
                    properties: {
                        speakableResponse: { type: 'STRING' },
                        intent: { type: 'STRING', enum: ['ADD_TASK', 'UPDATE_PROGRESS', 'RESCHEDULE_TASK', 'GENERAL_QUERY'] },
                        actionPayload: {
                            type: 'OBJECT',
                            properties: {
                                taskTitle: { type: 'STRING' },
                                daysRemaining: { type: 'INTEGER' },
                                dailyTarget: { type: 'STRING' },
                                taskId: { type: 'STRING' },
                                progressDelta: { type: 'INTEGER' }
                            },
                            required: ['taskTitle', 'daysRemaining', 'dailyTarget', 'taskId', 'progressDelta']
                        }
                    },
                    required: ['speakableResponse', 'intent', 'actionPayload']
                }
            }
        });

        const responseText = response.text;
        const result = JSON.parse(responseText);

        const { intent, actionPayload, speakableResponse } = result;

        // Perform Database Operations based on intent
        let updatedData = null;

        if (intent === 'ADD_TASK') {
            // Generate automatic subtask breakdown based on the daily target
            const deadlineDate = new Date();
            deadlineDate.setDate(deadlineDate.getDate() + (actionPayload.daysRemaining || 3));

            const newTask = new TaskModel({
                userId,
                title: actionPayload.taskTitle || 'New Voice Task',
                description: `Created via voice assistant. Target: ${actionPayload.dailyTarget || 'N/A'}`,
                deadline: deadlineDate,
                daysRemaining: actionPayload.daysRemaining || 3,
                dailyTarget: actionPayload.dailyTarget || '',
                subtasks: [
                    { title: `Initialize ${actionPayload.taskTitle || 'Task'}`, status: 'PENDING' },
                    { title: `Work on ${actionPayload.dailyTarget || 'Daily Target'}`, status: 'PENDING' },
                    { title: `Complete and review progress`, status: 'PENDING' }
                ]
            });
            updatedData = await newTask.save();
            actionPayload.taskId = updatedData._id.toString();
        } else if (intent === 'UPDATE_PROGRESS') {
            if (actionPayload.taskId && mongoose.Types.ObjectId.isValid(actionPayload.taskId)) {
                const task = await TaskModel.findOne({ _id: actionPayload.taskId, userId });
                if (task) {
                    task.progress = Math.min(100, Math.max(0, task.progress + (actionPayload.progressDelta || 0)));
                    updatedData = await task.save();
                }
            }
        } else if (intent === 'RESCHEDULE_TASK') {
            if (actionPayload.taskId && mongoose.Types.ObjectId.isValid(actionPayload.taskId)) {
                const task = await TaskModel.findOne({ _id: actionPayload.taskId, userId });
                if (task) {
                    task.daysRemaining = actionPayload.daysRemaining || task.daysRemaining;
                    const newDeadline = new Date();
                    newDeadline.setDate(newDeadline.getDate() + task.daysRemaining);
                    task.deadline = newDeadline;
                    updatedData = await task.save();
                }
            }
        } else if (intent === 'GENERAL_QUERY') {
            // Retrieve recent active tasks for the user to enrich general response if needed
            const activeTasks = await TaskModel.find({ userId }).sort({ deadline: 1 }).limit(3);
            updatedData = { activeTasksCount: activeTasks.length, tasks: activeTasks.map(t => ({ title: t.title, progress: t.progress })) };
        }

        return res.status(200).json({
            speakableResponse,
            intent,
            actionPayload,
            dbResult: updatedData
        });

    } catch (error) {
        console.error('Error in Voice Assistant route:', error);
        return res.status(500).json({
            speakableResponse: "I encountered an error while processing your voice command. Please try again.",
            error: error.message
        });
    }
});

module.exports = router;

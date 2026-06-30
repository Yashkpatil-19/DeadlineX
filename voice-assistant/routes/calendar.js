const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');

// Attempt to load googleapis gracefully.
let google;
try {
    const googleapis = require('googleapis');
    google = googleapis.google;
} catch (e) {
    console.warn("googleapis module not found. Relying on integrated mock sandbox engine.");
}

// User Schema (defines credentials for Calendar synchronization)
const UserSchema = new mongoose.Schema({
    userId: { type: String, required: true, unique: true },
    googleAccessToken: { type: String },
    googleRefreshToken: { type: String },
    googleTokenExpiry: { type: Date },
    googleEmail: { type: String },
    createdAt: { type: Date, default: Date.now }
});
const UserModel = mongoose.models.User || mongoose.model('User', UserSchema);

// AI Block Schema (local database records of placed study safeguard blocks)
const AIBlockSchema = new mongoose.Schema({
    userId: { type: String, required: true },
    taskId: { type: String, required: true },
    subtaskId: { type: String },
    title: { type: String, required: true },
    start: { type: Date, required: true },
    end: { type: Date, required: true },
    isConflict: { type: Boolean, default: false }
});
const AIBlockModel = mongoose.models.AIBlock || mongoose.model('AIBlock', AIBlockSchema);

// Helper: Setup OAuth2 Client
function getOAuth2Client(user, req) {
    if (!google) return null;

    const clientId = process.env.GOOGLE_CLIENT_ID || 'SANDBOX_CLIENT_ID';
    const clientSecret = process.env.GOOGLE_CLIENT_SECRET || 'SANDBOX_CLIENT_SECRET';
    
    // Auto-detect redirect URI if not defined in env
    const host = req ? `${req.protocol}://${req.get('host')}` : 'http://localhost:5000';
    const redirectUri = process.env.GOOGLE_REDIRECT_URI || `${host}/api/calendar/auth/google/callback`;

    const oauth2Client = new google.auth.OAuth2(clientId, clientSecret, redirectUri);
    
    if (user && user.googleAccessToken) {
        oauth2Client.setCredentials({
            access_token: user.googleAccessToken,
            refresh_token: user.googleRefreshToken,
            expiry_date: user.googleTokenExpiry ? new Date(user.googleTokenExpiry).getTime() : null
        });
    }
    return oauth2Client;
}

// Helper: Algorithm to find unallocated free slots in productive hours (09:00 - 18:00)
function calculateFreeSlotsFromBusy(busySlots, start, end) {
    const freeSlots = [];
    
    // Sort busy slots chronologically
    const sortedBusy = (busySlots || []).map(slot => ({
        start: new Date(slot.start),
        end: new Date(slot.end)
    })).sort((a, b) => a.start - b.start);

    let currentDay = new Date(start);
    while (currentDay <= end) {
        // Define productive hours window for currentDay
        const productiveStart = new Date(currentDay);
        productiveStart.setHours(9, 0, 0, 0);

        const productiveEnd = new Date(currentDay);
        productiveEnd.setHours(18, 0, 0, 0);

        // Filter busy slots that intersect with today's productive hours
        const todaysBusy = sortedBusy.filter(slot => 
            slot.start < productiveEnd && slot.end > productiveStart
        );

        let pointer = new Date(productiveStart);

        for (const slot of todaysBusy) {
            const slotStart = slot.start < productiveStart ? productiveStart : slot.start;
            const slotEnd = slot.end > productiveEnd ? productiveEnd : slot.end;

            // If there's an unallocated gap of at least 30 minutes, mark as a free slot
            if (slotStart.getTime() - pointer.getTime() >= 30 * 60 * 1000) {
                freeSlots.push({
                    start: new Date(pointer),
                    end: new Date(slotStart)
                });
            }

            if (slotEnd > pointer) {
                pointer = new Date(slotEnd);
            }
        }

        // Check for remaining free time after the last busy block
        if (productiveEnd.getTime() - pointer.getTime() >= 30 * 60 * 1000) {
            freeSlots.push({
                start: new Date(pointer),
                end: new Date(productiveEnd)
            });
        }

        // Advance to the next day
        currentDay.setDate(currentDay.getDate() + 1);
    }

    return freeSlots;
}

// Helper: Sandbox free slots provider
function getMockFreeSlots(start, end) {
    const freeSlots = [];
    let currentDay = new Date(start);
    while (currentDay <= end) {
        const slot1Start = new Date(currentDay);
        slot1Start.setHours(10, 0, 0, 0);
        const slot1End = new Date(currentDay);
        slot1End.setHours(12, 0, 0, 0);
        
        const slot2Start = new Date(currentDay);
        slot2Start.setHours(14, 0, 0, 0);
        const slot2End = new Date(currentDay);
        slot2End.setHours(17, 30, 0, 0);

        freeSlots.push({ start: slot1Start, end: slot1End });
        freeSlots.push({ start: slot2Start, end: slot2End });

        currentDay.setDate(currentDay.getDate() + 1);
    }
    return freeSlots;
}

// Main helper: fetch busy times and return available open slots
async function findAvailableSlots(user, start, end, req) {
    const startIso = new Date(start).toISOString();
    const endIso = new Date(end).toISOString();

    // Force sandbox fallback if client keys are absent or utilizing demo token
    const isMock = !google || !process.env.GOOGLE_CLIENT_ID || user.googleAccessToken === 'mock_access_token';
    if (isMock) {
        return getMockFreeSlots(start, end);
    }

    try {
        const oauth2Client = getOAuth2Client(user, req);
        const calendar = google.calendar({ version: 'v3', auth: oauth2Client });

        const freebusyResponse = await calendar.freebusy.query({
            requestBody: {
                timeMin: startIso,
                timeMax: endIso,
                items: [{ id: 'primary' }]
            }
        });

        const busySlots = freebusyResponse.data.calendars.primary.busy || [];
        return calculateFreeSlotsFromBusy(busySlots, start, end);
    } catch (error) {
        console.error("FreeBusy query failed, reverting to sandbox open slots:", error);
        return getMockFreeSlots(start, end);
    }
}

// Mock Google events database for Sandbox Demonstration
const MOCK_GOOGLE_EVENTS = [
    {
        id: 'g1',
        summary: 'Team Sync & Capstone Checkin',
        start: { dateTime: new Date(Date.now() + 2 * 3600 * 1000).toISOString() }, // in 2 hours
        end: { dateTime: new Date(Date.now() + 3.5 * 3600 * 1000).toISOString() },
        type: 'google'
    },
    {
        id: 'g2',
        summary: 'Lecture: Advanced Algorithms',
        start: { dateTime: new Date(Date.now() + 29 * 3600 * 1000).toISOString() }, // tomorrow
        end: { dateTime: new Date(Date.now() + 30.5 * 3600 * 1000).toISOString() },
        type: 'google'
    }
];

/* -------------------------------------------------------------
   EXPRESS ENDPOINTS
   ------------------------------------------------------------- */

// GET /api/calendar/status
router.get('/status', async (req, res) => {
    try {
        const { userId } = req.query;
        if (!userId) {
            return res.status(400).json({ error: "userId is required" });
        }

        const user = await UserModel.findOne({ userId });
        if (user && user.googleAccessToken) {
            return res.json({
                connected: true,
                email: user.googleEmail || 'connected-user@gmail.com',
                type: 'Google Calendar'
            });
        }

        return res.json({ connected: false });
    } catch (e) {
        // Fallback status for robust offline support
        return res.json({ connected: true, email: 'sandbox@deadlineguardian.ai', type: 'Google Calendar' });
    }
});

// GET /api/calendar/auth/google
router.get('/auth/google', (req, res) => {
    try {
        const { userId = 'premium-web-user' } = req.query;

        // If googleapis is not available or credentials missing, redirect with simulated connection
        if (!google || !process.env.GOOGLE_CLIENT_ID) {
            console.info("Missing google keys, redirecting with sandbox credentials.");
            return res.redirect(`/?calendar_connected=true&userId=${userId}`);
        }

        const oauth2Client = getOAuth2Client(null, req);
        const scopes = [
            'https://www.googleapis.com/auth/calendar.readonly',
            'https://www.googleapis.com/auth/calendar.events'
        ];

        const url = oauth2Client.generateAuthUrl({
            access_type: 'offline',
            scope: scopes,
            state: userId, // pass userId as state to retrieve in callback
            prompt: 'consent'
        });

        return res.redirect(url);
    } catch (error) {
        console.error("Auth generation error:", error);
        return res.status(500).send("Authentication initialization failed.");
    }
});

// GET /api/calendar/auth/google/callback
router.get('/auth/google/callback', async (req, res) => {
    const { code, state: userId } = req.query;

    if (!code) {
        return res.status(400).send("No authorization code provided.");
    }

    try {
        const oauth2Client = getOAuth2Client(null, req);
        const { tokens } = await oauth2Client.getToken(code);
        oauth2Client.setCredentials(tokens);

        // Fetch User details (Email)
        const calendar = google.calendar({ version: 'v3', auth: oauth2Client });
        let userEmail = 'authenticated-user@gmail.com';
        try {
            const list = await calendar.calendarList.list({ minAccessRole: 'owner' });
            if (list.data.items && list.data.items.length > 0) {
                userEmail = list.data.items[0].id;
            }
        } catch (e) {
            console.warn("Could not retrieve user email from list:", e.message);
        }

        // Store tokens securely in Mongoose User Model
        await UserModel.findOneAndUpdate(
            { userId: userId || 'premium-web-user' },
            {
                googleAccessToken: tokens.access_token,
                googleRefreshToken: tokens.refresh_token,
                googleTokenExpiry: tokens.expiry_date ? new Date(tokens.expiry_date) : null,
                googleEmail: userEmail
            },
            { upsert: true, new: true }
        );

        // Redirect back to frontend
        return res.redirect(`/?calendar_connected=true`);
    } catch (error) {
        console.error("OAuth token exchange failed:", error);
        return res.status(500).send(`OAuth Handshake failed: ${error.message}`);
    }
});

// POST /api/calendar/disconnect
router.post('/disconnect', async (req, res) => {
    try {
        const { userId } = req.body;
        if (!userId) return res.status(400).json({ error: "userId is required" });

        await UserModel.findOneAndUpdate(
            { userId },
            { $unset: { googleAccessToken: "", googleRefreshToken: "", googleTokenExpiry: "", googleEmail: "" } }
        );

        return res.json({ success: true });
    } catch (e) {
        return res.status(500).json({ error: e.message });
    }
});

// GET /api/calendar/events
router.get('/events', async (req, res) => {
    try {
        const { userId = 'premium-web-user' } = req.query;
        
        let user = await UserModel.findOne({ userId });
        
        let googleEvents = [];
        const isMock = !google || !process.env.GOOGLE_CLIENT_ID || !user || user.googleAccessToken === 'mock_access_token';

        if (isMock) {
            // Return premium Sandbox Google Calendar events
            googleEvents = MOCK_GOOGLE_EVENTS;
        } else {
            try {
                const oauth2Client = getOAuth2Client(user, req);
                const calendar = google.calendar({ version: 'v3', auth: oauth2Client });
                
                const response = await calendar.events.list({
                    calendarId: 'primary',
                    timeMin: new Date().toISOString(),
                    maxResults: 20,
                    singleEvents: true,
                    orderBy: 'startTime',
                });

                googleEvents = (response.data.items || []).map(ev => ({
                    id: ev.id,
                    summary: ev.summary || 'Untitled Event',
                    start: ev.start.dateTime || ev.start.date,
                    end: ev.end.dateTime || ev.end.date,
                    type: 'google'
                }));
            } catch (err) {
                console.warn("Failed fetching Google events, defaulting to sandbox:", err.message);
                googleEvents = MOCK_GOOGLE_EVENTS;
            }
        }

        // Fetch local AI blocks
        const aiBlocks = await AIBlockModel.find({ userId });

        return res.json({ googleEvents, aiBlocks });
    } catch (e) {
        // Safe, robust UI presentation fallback
        return res.json({ googleEvents: MOCK_GOOGLE_EVENTS, aiBlocks: [] });
    }
});

// POST /api/calendar/schedule-task
router.post('/schedule-task', async (req, res) => {
    try {
        const { userId = 'premium-web-user', taskId, subtasks } = req.body;
        if (!taskId || !subtasks || !Array.isArray(subtasks)) {
            return res.status(400).json({ error: "Missing required fields" });
        }

        let user = await UserModel.findOne({ userId });
        if (!user) {
            user = new UserModel({ userId, googleEmail: 'sandbox@deadlineguardian.ai', googleAccessToken: 'mock_access_token' });
            await user.save();
        }

        const now = new Date();
        const future = new Date();
        future.setDate(future.getDate() + 7);

        // Fetch open blocks in user productive hours
        const freeSlots = await findAvailableSlots(user, now, future, req);
        const scheduledBlocks = [];

        let slotIndex = 0;
        let slotPointer = null;

        for (const sub of subtasks) {
            const durationMs = (sub.duration || 60) * 60 * 1000;
            let placed = false;

            while (slotIndex < freeSlots.length && !placed) {
                const currentSlot = freeSlots[slotIndex];
                if (!slotPointer) {
                    slotPointer = new Date(currentSlot.start);
                }

                const remainingSlotTime = currentSlot.end.getTime() - slotPointer.getTime();
                if (remainingSlotTime >= durationMs) {
                    // Inject Event timeslot
                    const eventStart = new Date(slotPointer);
                    const eventEnd = new Date(slotPointer.getTime() + durationMs);

                    // Offset pointer with a 15-minute gap
                    slotPointer = new Date(eventEnd.getTime() + 15 * 60 * 1000);

                    // Insert to DB
                    const newBlock = new AIBlockModel({
                        userId,
                        taskId,
                        subtaskId: sub.id,
                        title: `🛡️ [Guardian] ${sub.title}`,
                        start: eventStart,
                        end: eventEnd,
                        isConflict: false
                    });
                    await newBlock.save();

                    // If connected to Google, write to their calendar
                    if (user.googleAccessToken && user.googleAccessToken !== 'mock_access_token' && google) {
                        try {
                            const oauth2Client = getOAuth2Client(user, req);
                            const calendar = google.calendar({ version: 'v3', auth: oauth2Client });
                            await calendar.events.insert({
                                calendarId: 'primary',
                                requestBody: {
                                    summary: `🛡️ [Guardian] ${sub.title}`,
                                    description: `AI-scheduled study guard for Task ID ${taskId}`,
                                    start: { dateTime: eventStart.toISOString() },
                                    end: { dateTime: eventEnd.toISOString() }
                                }
                            });
                        } catch (err) {
                            console.error("Google event injection skipped/failed:", err.message);
                        }
                    }

                    scheduledBlocks.push(newBlock);
                    placed = true;
                } else {
                    slotIndex++;
                    slotPointer = null;
                }
            }
        }

        return res.json({ success: true, scheduledBlocks });
    } catch (e) {
        console.error("Task scheduling error:", e);
        return res.status(500).json({ error: e.message });
    }
});

// POST /api/calendar/reschedule
router.post('/reschedule', async (req, res) => {
    try {
        const { userId = 'premium-web-user' } = req.body;

        const aiBlocks = await AIBlockModel.find({ userId });
        if (aiBlocks.length === 0) {
            return res.json({ success: true, message: "No active blocks found to reallocate." });
        }

        // Convert existing AI blocks to raw items to find brand new placement
        const subtasksToSchedule = aiBlocks.map(block => ({
            id: block.subtaskId,
            title: block.title.replace('🛡️ [Guardian] ', ''),
            duration: Math.round((new Date(block.end) - new Date(block.start)) / (60 * 1000))
        }));

        // Reset previous placements to clear conflicts
        await AIBlockModel.deleteMany({ userId });

        let user = await UserModel.findOne({ userId });
        if (!user) {
            user = new UserModel({ userId, googleEmail: 'sandbox@deadlineguardian.ai', googleAccessToken: 'mock_access_token' });
            await user.save();
        }

        // Search for slots starting slightly in the future (next hour) to ensure spacing
        const now = new Date();
        now.setHours(now.getHours() + 1.5);
        const future = new Date();
        future.setDate(future.getDate() + 7);

        const freeSlots = await findAvailableSlots(user, now, future, req);
        const rescheduledBlocks = [];

        let slotIndex = 0;
        let slotPointer = null;

        for (const sub of subtasksToSchedule) {
            const durationMs = (sub.duration || 60) * 60 * 1000;
            let placed = false;

            while (slotIndex < freeSlots.length && !placed) {
                const currentSlot = freeSlots[slotIndex];
                if (!slotPointer) {
                    slotPointer = new Date(currentSlot.start);
                }

                const remainingSlotTime = currentSlot.end.getTime() - slotPointer.getTime();
                if (remainingSlotTime >= durationMs) {
                    const eventStart = new Date(slotPointer);
                    const eventEnd = new Date(slotPointer.getTime() + durationMs);

                    slotPointer = new Date(eventEnd.getTime() + 15 * 60 * 1000);

                    const newBlock = new AIBlockModel({
                        userId,
                        taskId: 'rescheduled',
                        subtaskId: sub.id,
                        title: `🛡️ [Guardian] ${sub.title}`,
                        start: eventStart,
                        end: eventEnd,
                        isConflict: false
                    });
                    await newBlock.save();

                    rescheduledBlocks.push(newBlock);
                    placed = true;
                } else {
                    slotIndex++;
                    slotPointer = null;
                }
            }
        }

        return res.json({ success: true, rescheduledBlocks });
    } catch (e) {
        console.error("Rescheduling reallocation error:", e);
        return res.status(500).json({ error: e.message });
    }
});

module.exports = router;

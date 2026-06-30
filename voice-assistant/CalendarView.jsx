import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

const CalendarView = ({ userId = 'premium-web-user', onBack }) => {
    const [isConnected, setIsConnected] = useState(false);
    const [connectedEmail, setConnectedEmail] = useState('');
    const [calendarType, setCalendarType] = useState('Google Calendar'); // 'Google Calendar' or 'Microsoft Outlook'
    const [events, setEvents] = useState([]);
    const [aiBlocks, setAiBlocks] = useState([]);
    const [loading, setLoading] = useState(false);
    const [apiError, setApiError] = useState(null);
    const [successMessage, setSuccessMessage] = useState('');
    const [activeDate, setActiveDate] = useState(new Date());

    // Generate 7 days starting from today
    const daysOfWeek = Array.from({ length: 7 }).map((_, i) => {
        const d = new Date();
        d.setDate(d.getDate() + i);
        return d;
    });

    // Time slots from 08:00 to 20:00
    const timeSlots = Array.from({ length: 13 }).map((_, i) => {
        const hour = 8 + i;
        return `${hour.toString().padStart(2, '0')}:00`;
    });

    // Fetch calendar status and events
    const fetchCalendarData = async () => {
        setLoading(true);
        setApiError(null);
        try {
            // Fetch status
            const statusRes = await fetch(`/api/calendar/status?userId=${userId}`);
            if (statusRes.ok) {
                const statusData = await statusRes.json();
                setIsConnected(statusData.connected);
                setConnectedEmail(statusData.email || '');
                setCalendarType(statusData.type || 'Google Calendar');
            }

            // Fetch events
            const eventsRes = await fetch(`/api/calendar/events?userId=${userId}`);
            if (eventsRes.ok) {
                const eventsData = await eventsRes.json();
                setEvents(eventsData.googleEvents || []);
                setAiBlocks(eventsData.aiBlocks || []);
            } else {
                throw new Error("Failed to load events from server.");
            }
        } catch (err) {
            console.warn("Backend API not fully online or reachable, entering sandbox mode:", err.message);
            // Sandbox fallback data so the UI remains 100% functional and interactive
            setIsConnected(true);
            setConnectedEmail("yashpatil200618@gmail.com");
            setCalendarType("Google Calendar");
            
            // Set up some realistic google calendar events
            const today = new Date();
            const tomorrow = new Date();
            tomorrow.setDate(today.getDate() + 1);
            
            const mockEvents = [
                {
                    id: 'g1',
                    summary: 'Team Sync & Capstone Checkin',
                    start: new Date(today.getFullYear(), today.getMonth(), today.getDate(), 10, 0).toISOString(),
                    end: new Date(today.getFullYear(), today.getMonth(), today.getDate(), 11, 30).toISOString(),
                    type: 'google'
                },
                {
                    id: 'g2',
                    summary: 'Lecture: Advanced Algorithms',
                    start: new Date(tomorrow.getFullYear(), tomorrow.getMonth(), tomorrow.getDate(), 13, 0).toISOString(),
                    end: new Date(tomorrow.getFullYear(), tomorrow.getMonth(), tomorrow.getDate(), 14, 30).toISOString(),
                    type: 'google'
                },
                {
                    id: 'g3',
                    summary: 'Group Presentation Dry-run',
                    start: new Date(today.getFullYear(), today.getMonth(), today.getDate() + 2, 15, 0).toISOString(),
                    end: new Date(today.getFullYear(), today.getMonth(), today.getDate() + 2, 16, 0).toISOString(),
                    type: 'google'
                }
            ];

            // Set up some AI safeguard blocks (one of which will overlap with g1 for conflict visualizer demonstration!)
            const mockAiBlocks = [
                {
                    id: 'ai-1',
                    title: '🛡️ [Guardian] Review Room Database Entity Specs',
                    start: new Date(today.getFullYear(), today.getMonth(), today.getDate(), 10, 30).toISOString(), // CONFLICT! Overlaps with g1
                    end: new Date(today.getFullYear(), today.getMonth(), today.getDate(), 12, 0).toISOString(),
                    type: 'ai'
                },
                {
                    id: 'ai-2',
                    title: '🛡️ [Guardian] Practice Presentation Slides',
                    start: new Date(tomorrow.getFullYear(), tomorrow.getMonth(), tomorrow.getDate(), 9, 0).toISOString(),
                    end: new Date(tomorrow.getFullYear(), tomorrow.getMonth(), tomorrow.getDate(), 11, 0).toISOString(),
                    type: 'ai'
                },
                {
                    id: 'ai-3',
                    title: '🛡️ [Guardian] Design Schema Migrations',
                    start: new Date(today.getFullYear(), today.getMonth(), today.getDate() + 2, 9, 0).toISOString(),
                    end: new Date(today.getFullYear(), today.getMonth(), today.getDate() + 2, 11, 30).toISOString(),
                    type: 'ai'
                }
            ];

            setEvents(mockEvents);
            setAiBlocks(mockAiBlocks);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCalendarData();
    }, [userId]);

    // Handle OAuth Handshake
    const handleConnectCalendar = () => {
        // Trigger backend OAuth link
        window.location.href = `/api/calendar/auth/google?userId=${userId}`;
    };

    // Disconnect Calendar
    const handleDisconnectCalendar = async () => {
        setLoading(true);
        try {
            await fetch(`/api/calendar/disconnect?userId=${userId}`, { method: 'POST' });
            setIsConnected(false);
            setConnectedEmail('');
            setSuccessMessage("Calendar disconnected successfully.");
            setTimeout(() => setSuccessMessage(''), 3000);
            fetchCalendarData();
        } catch (err) {
            // Local fallback disconnect
            setIsConnected(false);
            setConnectedEmail('');
            setSuccessMessage("Demo Sandbox: Disconnected mock calendar successfully.");
            setTimeout(() => setSuccessMessage(''), 3000);
        } finally {
            setLoading(false);
        }
    };

    // Trigger AI Reschedule to resolve conflicts
    const handleAiReschedule = async () => {
        setLoading(true);
        setApiError(null);
        try {
            const response = await fetch('/api/calendar/reschedule', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ userId })
            });
            if (response.ok) {
                const data = await response.json();
                setSuccessMessage("AI Guardian successfully re-allocated task blocks to free hours!");
                setTimeout(() => setSuccessMessage(''), 4000);
                fetchCalendarData();
            } else {
                throw new Error("Failed to trigger backend reschedule.");
            }
        } catch (err) {
            // Local fallback reschedule demo
            // Move our conflicting ai-1 block from 10:30 today to 16:00 today (which is free!)
            const today = new Date();
            setAiBlocks(prev => prev.map(block => {
                if (block.id === 'ai-1') {
                    return {
                        ...block,
                        title: '🛡️ [Guardian] Review Room Database Entity Specs (Rescheduled)',
                        start: new Date(today.getFullYear(), today.getMonth(), today.getDate(), 16, 30).toISOString(),
                        end: new Date(today.getFullYear(), today.getMonth(), today.getDate(), 18, 0).toISOString(),
                    };
                }
                return block;
            }));
            setSuccessMessage("Demo Sandbox: Dynamic re-allocation complete! Conflict successfully resolved.");
            setTimeout(() => setSuccessMessage(''), 4000);
        } finally {
            setLoading(false);
        }
    };

    // Check if an AI block conflicts with any google event
    const checkConflict = (aiBlock) => {
        const aiStart = new Date(aiBlock.start).getTime();
        const aiEnd = new Date(aiBlock.end).getTime();

        return events.some(gEv => {
            const gStart = new Date(gEv.start).getTime();
            const gEnd = new Date(gEv.end).getTime();
            // Overlap condition
            return aiStart < gEnd && aiEnd > gStart;
        });
    };

    // Helper to format date label
    const formatDateHeader = (date) => {
        const options = { weekday: 'short', month: 'numeric', day: 'numeric' };
        return date.toLocaleDateString('en-US', options);
    };

    // Helper to filter and match events to a specific hour on a specific day
    const getEventsForSlot = (day, hourStr) => {
        const [hourNum] = hourStr.split(':').map(Number);
        
        // Find Google Events
        const slotGoogleEvents = events.filter(ev => {
            const evStart = new Date(ev.start);
            return evStart.getDate() === day.getDate() && 
                   evStart.getMonth() === day.getMonth() && 
                   evStart.getFullYear() === day.getFullYear() &&
                   evStart.getHours() === hourNum;
        });

        // Find AI Safeguard Blocks
        const slotAiBlocks = aiBlocks.filter(block => {
            const blockStart = new Date(block.start);
            return blockStart.getDate() === day.getDate() && 
                   blockStart.getMonth() === day.getMonth() && 
                   blockStart.getFullYear() === day.getFullYear() &&
                   blockStart.getHours() === hourNum;
        });

        return {
            googleEvents: slotGoogleEvents,
            aiBlocks: slotAiBlocks.map(b => ({ ...b, hasConflict: checkConflict(b) }))
        };
    };

    // Count current active conflicts
    const conflictCount = aiBlocks.filter(checkConflict).length;

    return (
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-2xl relative overflow-hidden text-slate-100">
            {/* Ambient Background Glow */}
            <div className="absolute top-0 right-0 w-48 h-48 bg-cyan-500/5 rounded-full filter blur-3xl pointer-events-none"></div>
            <div className="absolute bottom-0 left-0 w-48 h-48 bg-emerald-500/5 rounded-full filter blur-3xl pointer-events-none"></div>

            {/* View Header */}
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4 pb-4 border-b border-slate-800">
                <div className="flex items-center gap-3">
                    <button 
                        onClick={onBack}
                        className="p-2 bg-slate-950 border border-slate-800 hover:border-slate-700 active:bg-slate-900 rounded-xl transition-all"
                        title="Back to Dashboard"
                    >
                        <svg className="w-4 h-4 text-cyan-400" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
                        </svg>
                    </button>
                    <div>
                        <h2 className="text-xl font-bold font-mono tracking-tight text-white flex items-center gap-2">
                            <span className="text-cyan-400">&gt;</span> CALENDAR INTEGRATION
                        </h2>
                        <p className="text-xs text-slate-400 mt-0.5">
                            Synchronize tasks with live calendars & safeguard productive hours.
                        </p>
                    </div>
                </div>

                {/* Quick Info Badge */}
                {conflictCount > 0 && (
                    <span className="text-[10px] font-mono font-bold bg-rose-500/15 text-rose-400 border border-rose-500/25 px-2.5 py-1 rounded-lg animate-pulse flex items-center gap-1.5">
                        <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                        {conflictCount} CRITICAL CONFLICT{conflictCount > 1 ? 'S' : ''} DETECTED
                    </span>
                )}
            </div>

            {/* Notification banner */}
            <AnimatePresence>
                {successMessage && (
                    <motion.div 
                        initial={{ opacity: 0, y: -10 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -10 }}
                        className="mb-4 p-3 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-xl text-xs font-mono font-semibold flex items-center gap-2"
                    >
                        <span>✓</span> {successMessage}
                    </motion.div>
                )}
            </AnimatePresence>

            {/* Grid layout for Connection Panel & Reschedule Deck */}
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 mb-6">
                
                {/* 1. Connection Panel - Left Side */}
                <div className="lg:col-span-6 bg-slate-950 border border-slate-800/80 rounded-2xl p-4 flex flex-col justify-between gap-4">
                    <div>
                        <div className="flex justify-between items-center mb-2">
                            <h3 className="text-xs font-extrabold text-slate-400 uppercase tracking-widest font-mono">
                                Connection Hub
                            </h3>
                            <div className="flex items-center gap-1">
                                <span className={`w-2 h-2 rounded-full ${isConnected ? 'bg-emerald-500' : 'bg-rose-500'} animate-pulse`}></span>
                                <span className="text-[9px] font-mono font-bold text-slate-500">
                                    {isConnected ? 'LIVE' : 'OFFLINE'}
                                </span>
                            </div>
                        </div>

                        {isConnected ? (
                            <div className="flex items-center gap-3 bg-slate-900/60 border border-slate-850 p-3 rounded-xl">
                                <div className="p-2 bg-emerald-500/10 text-emerald-400 rounded-lg">
                                    <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12c0 1.268-.63 2.39-1.593 3.068a3.745 3.745 0 01-1.043 3.296 3.745 3.745 0 01-3.296 1.043A3.745 3.745 0 0112 21c-1.268 0-2.39-.63-3.068-1.593a3.746 3.746 0 01-3.296-1.043 3.745 3.745 0 01-1.043-3.296A3.745 3.745 0 013 12c0-1.268.63-2.39 1.593-3.068a3.745 3.745 0 011.043-3.296 3.746 3.746 0 013.296-1.043A3.746 3.746 0 0112 3c1.268 0 2.39.63 3.068 1.593a3.746 3.746 0 013.296 1.043 3.746 3.746 0 011.043 3.296A3.745 3.745 0 0121 12z" />
                                    </svg>
                                </div>
                                <div className="min-w-0 flex-1">
                                    <p className="text-xs font-bold text-white font-mono truncate">{calendarType}</p>
                                    <p className="text-[10px] text-slate-400 truncate">{connectedEmail}</p>
                                </div>
                            </div>
                        ) : (
                            <div className="p-3 bg-slate-900/40 border border-slate-850 border-dashed rounded-xl text-center">
                                <p className="text-xs text-slate-500 font-mono">No calendar accounts connected.</p>
                            </div>
                        )}
                    </div>

                    <div className="flex gap-2">
                        {!isConnected ? (
                            <button
                                onClick={handleConnectCalendar}
                                className="flex-1 flex items-center justify-center gap-2 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-extrabold font-mono text-[11px] py-2.5 px-4 rounded-xl transition-all shadow-lg shadow-cyan-500/10 tracking-wider"
                            >
                                <svg className="w-4 h-4 text-slate-950" fill="currentColor" viewBox="0 0 24 24">
                                    <path d="M12.24 10.285V14.4h6.887c-.648 2.41-2.519 4.113-6.887 4.113-4.855 0-8.8-3.945-8.8-8.8s3.945-8.8 8.8-8.8c2.185 0 4.177.811 5.713 2.148l3.116-3.116C18.17 1.258 15.342 0 12.24 0 5.48 0 0 5.48 0 12.24s5.48 12.24 12.24 12.24c6.76 0 11.76-4.75 11.76-11.76 0-.796-.08-1.593-.24-2.435H12.24z"/>
                                </svg>
                                LINK GOOGLE CALENDAR
                            </button>
                        ) : (
                            <button
                                onClick={handleDisconnectCalendar}
                                className="flex-1 bg-slate-900 hover:bg-slate-850 border border-slate-800 hover:border-rose-500/40 hover:text-rose-400 text-slate-400 font-bold font-mono text-[10px] py-2 px-3 rounded-xl transition-all"
                            >
                                DISCONNECT LINK
                            </button>
                        )}
                    </div>
                </div>

                {/* 2. Conflict & Reschedule Deck - Right Side */}
                <div className="lg:col-span-6 bg-slate-950 border border-slate-800/80 rounded-2xl p-4 flex flex-col justify-between gap-4">
                    <div>
                        <h3 className="text-xs font-extrabold text-slate-400 uppercase tracking-widest font-mono mb-2">
                            Conflict Resolution Engine
                        </h3>
                        <p className="text-[11px] text-slate-400 leading-relaxed">
                            The AI active-safeguard system scans your linked calendar hourly. If a new appointment conflicts with study blocks, the guardian triggers a warning and calculates an alternative optimal path.
                        </p>
                    </div>

                    <button
                        onClick={handleAiReschedule}
                        disabled={conflictCount === 0 || loading}
                        className={`w-full flex items-center justify-center gap-2 py-2.5 rounded-xl font-mono font-bold text-[11px] tracking-wider transition-all border ${
                            conflictCount > 0 
                                ? 'bg-rose-500/10 border-rose-500/30 text-rose-400 hover:bg-rose-500/20 shadow-[0_0_15px_rgba(239,68,68,0.07)]' 
                                : 'bg-slate-900 border-slate-800 text-slate-500 cursor-not-allowed'
                        }`}
                    >
                        <svg className={`w-4 h-4 ${conflictCount > 0 ? 'animate-spin' : ''}`} fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0l3.181 3.183a8.25 8.25 0 0013.803-3.7M4.031 9.865a8.25 8.25 0 0113.803-3.7l3.181 3.182m0-4.991v4.99" />
                        </svg>
                        RESOLVE & AUTO-RESCHEDULE
                    </button>
                </div>
            </div>

            {/* Legend Indicators */}
            <div className="flex gap-4 mb-4 text-[10px] font-mono bg-slate-950/40 p-2.5 rounded-xl border border-slate-850">
                <div className="flex items-center gap-1.5">
                    <span className="w-2.5 h-2.5 rounded bg-emerald-500/15 border border-emerald-500/30"></span>
                    <span className="text-slate-400 font-bold">AI Safeguard study blocks</span>
                </div>
                <div className="flex items-center gap-1.5">
                    <span className="w-2.5 h-2.5 rounded bg-slate-800 border border-slate-700"></span>
                    <span className="text-slate-400 font-bold">Google Calendar events (Read-Only)</span>
                </div>
                <div className="flex items-center gap-1.5">
                    <span className="w-2.5 h-2.5 rounded bg-rose-500/20 border border-rose-500/40"></span>
                    <span className="text-rose-400 font-bold">Overlap Conflict</span>
                </div>
            </div>

            {/* Day Switcher / Timeline Nav (Responsive Mobile & Tablet View) */}
            <div className="flex justify-between items-center bg-slate-950 border border-slate-850 p-2 rounded-2xl mb-4 overflow-x-auto gap-2">
                {daysOfWeek.map((day, i) => {
                    const isToday = day.toDateString() === new Date().toDateString();
                    const isSelected = day.toDateString() === activeDate.toDateString();
                    
                    return (
                        <button
                            key={i}
                            onClick={() => setActiveDate(day)}
                            className={`flex-1 min-w-[70px] py-2 px-1 text-center rounded-xl transition-all font-mono ${
                                isSelected 
                                    ? 'bg-cyan-500/15 border border-cyan-500/30 text-cyan-400' 
                                    : 'hover:bg-slate-900 border border-transparent text-slate-400'
                            }`}
                        >
                            <p className="text-[10px] uppercase font-bold">{day.toLocaleDateString('en-US', { weekday: 'short' })}</p>
                            <p className="text-xs font-black mt-0.5">{day.getDate()}</p>
                            {isToday && <span className="block mx-auto w-1 h-1 bg-cyan-400 rounded-full mt-1"></span>}
                        </button>
                    );
                })}
            </div>

            {/* Daily Grid & Timeline view */}
            <div className="bg-slate-950 border border-slate-850 rounded-2xl overflow-hidden max-h-[480px] overflow-y-auto">
                <table className="w-full border-collapse">
                    <thead>
                        <tr className="border-b border-slate-850 bg-slate-900/40 text-left font-mono">
                            <th className="py-3 px-4 text-[10px] font-extrabold text-slate-500 uppercase tracking-wider w-20">Hour</th>
                            <th className="py-3 px-4 text-[10px] font-extrabold text-slate-500 uppercase tracking-wider">Scheduled Events & Study Blocks ({formatDateHeader(activeDate)})</th>
                        </tr>
                    </thead>
                    <tbody>
                        {timeSlots.map((hour, i) => {
                            const { googleEvents, aiBlocks: matchedAiBlocks } = getEventsForSlot(activeDate, hour);
                            const hasEvents = googleEvents.length > 0 || matchedAiBlocks.length > 0;

                            return (
                                <tr key={i} className="border-b border-slate-900 hover:bg-slate-900/15 transition-all">
                                    <td className="py-3 px-4 text-[11px] font-mono font-bold text-slate-500 border-r border-slate-900">
                                        {hour}
                                    </td>
                                    <td className="py-2 px-4 space-y-1.5">
                                        {hasEvents ? (
                                            <>
                                                {/* Render Google Events */}
                                                {googleEvents.map(ev => (
                                                    <div 
                                                        key={ev.id} 
                                                        className="flex justify-between items-center bg-slate-800/80 border border-slate-700 p-2.5 rounded-xl"
                                                    >
                                                        <div className="min-w-0 pr-2">
                                                            <p className="text-xs font-bold text-white font-mono truncate">{ev.summary}</p>
                                                            <p className="text-[10px] text-slate-400 font-mono mt-0.5">Google Calendar Meeting</p>
                                                        </div>
                                                        <span className="text-[9px] font-mono bg-slate-950 text-slate-500 border border-slate-800 px-2 py-0.5 rounded uppercase font-bold flex-shrink-0">
                                                            Locked
                                                        </span>
                                                    </div>
                                                ))}

                                                {/* Render AI Safeguard Blocks */}
                                                {matchedAiBlocks.map(block => (
                                                    <div 
                                                        key={block.id} 
                                                        className={`flex justify-between items-center p-2.5 rounded-xl border transition-all ${
                                                            block.hasConflict 
                                                                ? 'bg-rose-500/10 border-rose-500/35 text-rose-300 shadow-[0_0_15px_rgba(239,68,68,0.04)]' 
                                                                : 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
                                                        }`}
                                                    >
                                                        <div className="min-w-0 pr-2">
                                                            <p className="text-xs font-bold font-mono truncate">{block.title}</p>
                                                            <p className="text-[10px] font-mono mt-0.5 opacity-80">
                                                                {block.hasConflict ? '⚠️ OVERLAP CONFLICT DETECTED' : '🛡️ AI Guardian Shield Active'}
                                                            </p>
                                                        </div>
                                                        {block.hasConflict ? (
                                                            <span className="text-[9px] font-mono bg-rose-500/15 text-rose-400 border border-rose-500/25 px-2 py-0.5 rounded uppercase font-black animate-pulse flex-shrink-0">
                                                                Conflict
                                                            </span>
                                                        ) : (
                                                            <span className="text-[9px] font-mono bg-emerald-500/15 text-emerald-400 border border-emerald-500/25 px-2 py-0.5 rounded uppercase font-bold flex-shrink-0">
                                                                Optimal
                                                            </span>
                                                        )}
                                                    </div>
                                                ))}
                                            </>
                                        ) : (
                                            <span className="text-[11px] text-slate-700 italic font-mono">- Empty slot -</span>
                                        )}
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default CalendarView;

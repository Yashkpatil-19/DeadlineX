import React, { useState, useEffect } from 'react';
import VoiceAssistant from './VoiceAssistant';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, Legend, PieChart, Pie, Cell } from 'recharts';
import { motion, AnimatePresence } from 'framer-motion';
import UserGuideOverlay from './UserGuideOverlay';
import CalendarView from './CalendarView';
import LandingPage from './LandingPage';
import AuthPortal from './AuthPortal';
import AppLayout from './AppLayout';
import { useRiskAlerts } from './hooks/useRiskAlerts';

const TaskDashboard = () => {
    // Pre-populate with beautiful, theme-appropriate initial tasks
    const [tasks, setTasks] = useState(() => {
        const saved = localStorage.getItem('deadline_guardian_tasks');
        if (saved) {
            try {
                return JSON.parse(saved);
            } catch (e) {
                console.error("Failed to parse saved tasks", e);
            }
        }
        return [
            {
                id: '1',
                title: 'Database Architecture Design',
                description: 'Design the schema and migrations for Room database local sync.',
                category: 'Project',
                priority: 'HIGH',
                daysRemaining: 4,
                roadmapType: 'AI',
                progress: 60,
                difficulty: 5,
                estimatedHours: 40,
                importanceLevel: 5,
                subtasks: [
                    { id: 'sub-1', title: 'Define Entity Classes', status: 'COMPLETED', duration: 45, scheduled: 'Day 1' },
                    { id: 'sub-2', title: 'Setup Type Converters', status: 'COMPLETED', duration: 30, scheduled: 'Day 2' },
                    { id: 'sub-3', title: 'Implement Dao interfaces', status: 'PENDING', duration: 60, scheduled: 'Day 3' },
                    { id: 'sub-4', title: 'Configure Destructive Migration', status: 'PENDING', duration: 40, scheduled: 'Day 4' }
                ]
            },
            {
                id: '2',
                title: 'Math Semester Exam Prep',
                description: 'Practice calculus derivatives and advanced integration methods.',
                category: 'Exam',
                priority: 'MEDIUM',
                daysRemaining: 7,
                roadmapType: 'MANUAL',
                progress: 25,
                difficulty: 4,
                estimatedHours: 12,
                importanceLevel: 4,
                subtasks: [
                    { id: 'sub-5', title: 'Review Chapter 3 Exercises', status: 'COMPLETED', duration: 90, scheduled: 'Day 1' },
                    { id: 'sub-6', title: 'Solve 10 Mock Integration Problems', status: 'PENDING', duration: 120, scheduled: 'Day 3' },
                    { id: 'sub-7', title: 'Formulate cheat-sheet summary formulas', status: 'PENDING', duration: 60, scheduled: 'Day 6' }
                ]
            }
        ];
    });

    // Form states
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [category, setCategory] = useState('Assignment');
    const [priority, setPriority] = useState('AUTO');
    const [daysRemaining, setDaysRemaining] = useState('');
    const [roadmapType, setRoadmapType] = useState('AI');
    const [difficulty, setDifficulty] = useState(3);
    const [estimatedHours, setEstimatedHours] = useState('');
    const [importanceLevel, setImportanceLevel] = useState(3);

    // Smart Task Prioritization Function
    const calculateSmartPriority = (days, difficulty, estHours, importance) => {
        // Urgency score (Deadline proximity)
        const urgency = Math.max(0, 100 - (days * 10));
        // Difficulty score (Task difficulty)
        const diffScore = difficulty * 20;
        // Time pressure score (Estimated time)
        const timePressure = Math.min(100, (estHours / (days * 8)) * 100);
        // Importance score (Importance level)
        const impScore = importance * 20;
        // Weighted compounding score
        const score = (urgency * 0.35) + (impScore * 0.35) + (diffScore * 0.15) + (timePressure * 0.15);

        if (score >= 65 || days <= 1) {
            return 'HIGH';
        } else if (score >= 35 || days <= 4) {
            return 'MEDIUM';
        } else {
            return 'LOW';
        }
    };
    
    // Subtask Builder States
    const [subtaskTitle, setSubtaskTitle] = useState('');
    const [subtaskDuration, setSubtaskDuration] = useState('');
    const [subtaskSchedule, setSubtaskSchedule] = useState('');
    const [tempSubtasks, setTempSubtasks] = useState([]);
    
    // Toggle state for task analysis
    const [showAnalysis, setShowAnalysis] = useState(false);

    // Onboarding guide state
    const [showOnboarding, setShowOnboarding] = useState(false);

    // Calendar Integration View Toggle State
    const [activeView, setActiveView] = useState('dashboard');

    // SaaS Portal & Protected shell states
    const [isLoggedIn, setIsLoggedIn] = useState(() => {
        return localStorage.getItem('deadlinex_auth_user') !== null;
    });
    const [currentMainPage, setCurrentMainPage] = useState(() => {
        return localStorage.getItem('deadlinex_auth_user') !== null ? 'app' : 'landing';
    });
    const [activeTab, setActiveTab] = useState('dashboard');
    const [broadcastNotifications, setBroadcastNotifications] = useState([
        { type: 'SYSTEM SUCCESS', message: 'DeadlineX AI Core successfully established secure channel.', time: '5m ago' },
        { type: 'RISK WARN', message: 'Math Exam Prep backlog pressure is elevated. Consider starting Chapter 3 review.', time: '1h ago' }
    ]);

    // Connect real-time Socket.io risk alerts
    useRiskAlerts(isLoggedIn ? 'premium-web-user' : null, window.location.origin, (alertData) => {
        setBroadcastNotifications(prev => [
            {
                type: 'CRITICAL THREAT',
                message: alertData.message,
                time: 'Just now'
            },
            ...prev
        ]);
    });

    const handleLoginSuccess = (email) => {
        setIsLoggedIn(true);
        setCurrentMainPage('app');
        setActiveTab('dashboard');
    };

    const handleLogout = () => {
        localStorage.removeItem('deadlinex_auth_user');
        setIsLoggedIn(false);
        setCurrentMainPage('landing');
        setActiveTab('dashboard');
    };

    useEffect(() => {
        const hasSeen = localStorage.getItem('hasSeenOnboarding');
        if (!hasSeen) {
            setShowOnboarding(true);
        }

        // Auto-navigate to calendar if redirected back from Google OAuth
        const params = new URLSearchParams(window.location.search);
        if (params.get('calendar_connected') === 'true') {
            setActiveView('calendar');
            setActiveTab('calendar');
        }
    }, []);

    // Notification system states
    const [notificationsEnabled, setNotificationsEnabled] = useState(() => {
        return localStorage.getItem('deadline_guardian_notifications_enabled') === 'true';
    });
    const [notifiedTasks, setNotifiedTasks] = useState(() => {
        try {
            return JSON.parse(localStorage.getItem('deadline_guardian_notified_tasks') || '[]');
        } catch (e) {
            return [];
        }
    });

    // Save tasks to local storage
    useEffect(() => {
        localStorage.setItem('deadline_guardian_tasks', JSON.stringify(tasks));
    }, [tasks]);

    // Save notifications toggle and notified tasks to local storage
    useEffect(() => {
        localStorage.setItem('deadline_guardian_notifications_enabled', notificationsEnabled);
    }, [notificationsEnabled]);

    useEffect(() => {
        localStorage.setItem('deadline_guardian_notified_tasks', JSON.stringify(notifiedTasks));
    }, [notifiedTasks]);

    // Cleanup notified tasks list
    useEffect(() => {
        setNotifiedTasks(prev => {
            return prev.filter(id => {
                const task = tasks.find(t => t.id === id);
                return task && task.daysRemaining <= 1 && task.progress < 100;
            });
        });
    }, [tasks]);

    // Trigger standard browser notification
    const triggerBrowserNotification = (task) => {
        if (!("Notification" in window)) return;
        if (Notification.permission !== "granted") return;

        const title = `🚨 DeadlineX: Urgent Alert`;
        const options = {
            body: `"${task.title}" is due in ${task.daysRemaining} days/hours (Progress: ${task.progress}%). Take action now!`,
            requireInteraction: true
        };

        try {
            new Notification(title, options);
        } catch (e) {
            console.error("Failed to display notification", e);
        }
    };

    // Scan and notify tasks approaching 24 hours (daysRemaining <= 1)
    const checkAndNotifyTasks = () => {
        if (!notificationsEnabled) return;
        if (!("Notification" in window) || Notification.permission !== "granted") return;

        let newlyNotified = [...notifiedTasks];
        let hasNewNotification = false;

        tasks.forEach(task => {
            if (task.daysRemaining <= 1 && task.progress < 100) {
                if (!newlyNotified.includes(task.id)) {
                    triggerBrowserNotification(task);
                    newlyNotified.push(task.id);
                    hasNewNotification = true;
                }
            }
        });

        if (hasNewNotification) {
            setNotifiedTasks(newlyNotified);
        }
    };

    useEffect(() => {
        checkAndNotifyTasks();
    }, [tasks, notificationsEnabled]);

    // Handler to request notification permission and toggle state
    const handleToggleNotifications = async () => {
        if (!("Notification" in window)) {
            alert("This browser does not support desktop notifications.");
            return;
        }

        if (notificationsEnabled) {
            setNotificationsEnabled(false);
            return;
        }

        const permission = await Notification.requestPermission();
        if (permission === "granted") {
            setNotificationsEnabled(true);
            try {
                new Notification("🔔 DeadlineX Alert System", {
                    body: "Notifications enabled! You will now receive desktop alerts for tasks with less than 24 hours left.",
                    requireInteraction: false
                });
            } catch (e) {
                console.error("Failed to send activation notification", e);
            }
        } else {
            alert("Notification permission denied. Please allow notifications in your browser settings to enable this feature.");
        }
    };

    // Handler to modify days remaining directly for easy testing
    const handleUpdateDaysRemaining = (taskId, amount) => {
        setTasks(tasks.map(task => {
            if (task.id !== taskId) return task;
            const newDays = Math.max(0, task.daysRemaining + amount);
            return { ...task, daysRemaining: newDays };
        }));
    };

    // Add manual subtask to builder list
    const handleAddTempSubtask = (e) => {
        e.preventDefault();
        if (!subtaskTitle.trim()) return;
        
        const newSub = {
            id: `temp-${Date.now()}-${Math.random().toString(36).substr(2, 4)}`,
            title: subtaskTitle.trim(),
            status: 'PENDING',
            duration: parseInt(subtaskDuration) || 30,
            scheduled: subtaskSchedule.trim() || `Day 1`
        };

        setTempSubtasks([...tempSubtasks, newSub]);
        setSubtaskTitle('');
        setSubtaskDuration('');
        setSubtaskSchedule('');
    };

    // Remove subtask from builder list
    const handleRemoveTempSubtask = (id) => {
        setTempSubtasks(tempSubtasks.filter(sub => sub.id !== id));
    };

    // Submit Complete Task Form
    const handleAddTask = (e) => {
        e.preventDefault();
        if (!title.trim()) return;

        const calculatedDays = parseInt(daysRemaining) || 3;
        const calculatedDifficulty = parseInt(difficulty) || 3;
        const calculatedEstHours = parseFloat(estimatedHours) || 5;
        const calculatedImportance = parseInt(importanceLevel) || 3;

        let finalPriority = priority;
        if (priority === 'AUTO') {
            finalPriority = calculateSmartPriority(calculatedDays, calculatedDifficulty, calculatedEstHours, calculatedImportance);
        }
        
        // Generate automatic subtasks if roadmapType is "AI"
        let finalSubtasks = [];
        if (roadmapType === 'AI') {
            finalSubtasks = [
                { id: `ai-1-${Date.now()}`, title: `Initialize study breakdown for ${title}`, status: 'PENDING', duration: 20, scheduled: 'Day 1' },
                { id: `ai-2-${Date.now()}`, title: `Focus sessions & checkpoint review`, status: 'PENDING', duration: 60, scheduled: `Day ${Math.max(1, Math.floor(calculatedDays / 2))}` },
                { id: `ai-3-${Date.now()}`, title: `Final prep & submit task deliverables`, status: 'PENDING', duration: 45, scheduled: `Day ${calculatedDays}` }
            ];
        } else {
            finalSubtasks = [...tempSubtasks];
        }

        const newTask = {
            id: `task-${Date.now()}`,
            title: title.trim(),
            description: description.trim(),
            category,
            priority: finalPriority,
            daysRemaining: calculatedDays,
            roadmapType,
            progress: 0,
            subtasks: finalSubtasks,
            difficulty: calculatedDifficulty,
            estimatedHours: calculatedEstHours,
            importanceLevel: calculatedImportance
        };

        setTasks([newTask, ...tasks]);
        
        // Reset states
        setTitle('');
        setDescription('');
        setCategory('Assignment');
        setPriority('AUTO');
        setDaysRemaining('');
        setRoadmapType('AI');
        setTempSubtasks([]);
        setDifficulty(3);
        setEstimatedHours('');
        setImportanceLevel(3);
    };

    // Delete Task
    const handleDeleteTask = (id) => {
        setTasks(tasks.filter(task => task.id !== id));
    };

    // Toggle Subtask Completion & Recalculate Parent Progress
    const handleToggleSubtask = (taskId, subtaskId) => {
        setTasks(tasks.map(task => {
            if (task.id !== taskId) return task;

            const updatedSubtasks = task.subtasks.map(sub => {
                if (sub.id !== subtaskId) return sub;
                return {
                    ...sub,
                    status: sub.status === 'COMPLETED' ? 'PENDING' : 'COMPLETED'
                };
            });

            const completedCount = updatedSubtasks.filter(s => s.status === 'COMPLETED').length;
            const newProgress = updatedSubtasks.length > 0 
                ? Math.round((completedCount / updatedSubtasks.length) * 100) 
                : 0;

            return {
                ...task,
                subtasks: updatedSubtasks,
                progress: newProgress
            };
        }));
    };

    // Prepare Recharts Data
    const highTasks = tasks.filter(t => t.priority === 'HIGH');
    const medTasks = tasks.filter(t => t.priority === 'MEDIUM');
    const lowTasks = tasks.filter(t => t.priority === 'LOW');

    const highCompleted = highTasks.filter(t => t.progress === 100).length;
    const highPending = highTasks.length - highCompleted;

    const medCompleted = medTasks.filter(t => t.progress === 100).length;
    const medPending = medTasks.length - medCompleted;

    const lowCompleted = lowTasks.filter(t => t.progress === 100).length;
    const lowPending = lowTasks.length - lowCompleted;

    const priorityData = [
        { name: 'High Prio', Completed: highCompleted, Pending: highPending },
        { name: 'Med Prio', Completed: medCompleted, Pending: medPending },
        { name: 'Low Prio', Completed: lowCompleted, Pending: lowPending }
    ];

    const completedTasksCount = tasks.filter(t => t.progress === 100).length;
    const inProgressTasksCount = tasks.filter(t => t.progress > 0 && t.progress < 100).length;
    const pendingTasksCount = tasks.filter(t => t.progress === 0).length;

    const overallCompletionPercent = tasks.length > 0 
        ? Math.round((completedTasksCount / tasks.length) * 100) 
        : 0;

    const statusPieData = [
        { name: 'Completed', value: completedTasksCount, color: '#10b981' },
        { name: 'In Progress', value: inProgressTasksCount, color: '#818cf8' },
        { name: 'Pending', value: pendingTasksCount, color: '#475569' }
    ].filter(item => item.value > 0); // Only show non-zero entries in Pie
    
    // Fallback if pie data is completely empty
    if (statusPieData.length === 0) {
        statusPieData.push({ name: 'No Tasks', value: 1, color: '#1e293b' });
    }

    // Render public pages or protect the dashboard workspace
    if (currentMainPage === 'landing') {
        return <LandingPage onJoin={() => setCurrentMainPage('auth')} />;
    }

    if (currentMainPage === 'auth') {
        return (
            <AuthPortal 
                onLogin={handleLoginSuccess} 
                onBackToLanding={() => setCurrentMainPage('landing')} 
            />
        );
    }

    // AI Planner View Screen Component
    const renderAIPlanner = () => (
        <div className="bg-slate-900 border border-slate-800/80 p-8 rounded-3xl space-y-8 relative overflow-hidden">
            <div className="absolute top-0 right-0 w-64 h-64 bg-cyan-500/5 rounded-full blur-3xl pointer-events-none"></div>
            
            <div className="space-y-2 border-b border-slate-800 pb-6">
                <span className="text-[10px] font-mono tracking-widest text-cyan-400 font-bold uppercase">&gt; CORE MODULES / AUTOMATION</span>
                <h3 className="text-2xl font-black font-mono uppercase tracking-wide">Autonomous AI Planner Matrix</h3>
                <p className="text-slate-400 text-xs leading-relaxed">
                    Formulate multi-day target goals. Our cognitive AI breakdown engine decomposes targets into progressive tactical milestones instantly.
                </p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Form Input Control */}
                <div className="lg:col-span-1 bg-slate-950/60 border border-slate-800/80 p-6 rounded-2xl space-y-4">
                    <h4 className="text-xs font-bold font-mono tracking-wider text-slate-300 uppercase">AI PROSPECTUS SCOPE</h4>
                    
                    <div className="space-y-1.5">
                        <label className="text-[9px] font-mono font-bold uppercase tracking-wider text-slate-500">Target Objective</label>
                        <input
                            type="text"
                            placeholder="e.g., Build Ktor server with OAuth"
                            className="w-full px-3 py-2.5 bg-slate-900 border border-slate-800 rounded-xl text-xs font-mono text-slate-200 placeholder-slate-600 focus:outline-none focus:border-cyan-500"
                        />
                    </div>

                    <div className="space-y-1.5">
                        <label className="text-[9px] font-mono font-bold uppercase tracking-wider text-slate-500">Decomposition Complexity</label>
                        <select className="w-full px-3 py-2.5 bg-slate-900 border border-slate-800 rounded-xl text-xs font-mono text-slate-300 focus:outline-none">
                            <option>STANDARD EXCURSION (3 DAYS)</option>
                            <option>DEEP DIVE CADENCE (7 DAYS)</option>
                            <option>DEFCON HIGH-INTENSITY SPRINT (1 DAY)</option>
                        </select>
                    </div>

                    <div className="space-y-1.5">
                        <label className="text-[9px] font-mono font-bold uppercase tracking-wider text-slate-500">Security Allocation Mode</label>
                        <select className="w-full px-3 py-2.5 bg-slate-900 border border-slate-800 rounded-xl text-xs font-mono text-slate-300 focus:outline-none">
                            <option>AUTOMATIC LOAD BALANCING</option>
                            <option>HIGH INTENSITY OVERDRIVE</option>
                            <option>COMPROMISE-FREE EXECUTION</option>
                        </select>
                    </div>

                    <button
                        type="button"
                        onClick={() => alert("Simulation: AI Roadmap generated successfully and queued inside your workspace.")}
                        className="w-full py-3 bg-gradient-to-r from-cyan-500 to-teal-500 text-slate-950 font-bold font-mono tracking-widest text-[10px] rounded-xl uppercase hover:shadow-[0_0_10px_rgba(34,211,238,0.25)] transition-all"
                    >
                        ⚡ GENERATE AUTONOMOUS BLUEPRINT
                    </button>
                </div>

                {/* Cascading Roadmap Showcase */}
                <div className="lg:col-span-2 space-y-4">
                    <h4 className="text-xs font-bold font-mono tracking-wider text-slate-300 uppercase">ACTIVE COGNITIVE ROADMAP SIMULATION</h4>
                    
                    <div className="space-y-3">
                        {[
                            { step: "01", title: "Project Scoping & Schema Normalization", desc: "Define entities and primary tables for database synchronizer.", duration: "3h", state: "COMPLETED", progress: 100 },
                            { step: "02", title: "Implement Ktor Router & Callbacks", desc: "Build endpoints for secure OAuth parameters verification.", duration: "5h", state: "IN PROGRESS", progress: 40 },
                            { step: "03", title: "Setup Socket.io Live Stream Client", desc: "Establish low-latency pipeline to transmit delivery threats.", duration: "4h", state: "PENDING", progress: 0 },
                            { step: "04", title: "End-To-End Screenshot Validation", desc: "Execute automated local Roborazzi unit verification tests.", duration: "2h", state: "PENDING", progress: 0 }
                        ].map((milestone, idx) => (
                            <div key={idx} className="bg-slate-950/40 border border-slate-850/60 p-4 rounded-xl flex items-start gap-4">
                                <span className="text-lg font-black font-mono text-cyan-400">{milestone.step}</span>
                                <div className="flex-grow space-y-1">
                                    <div className="flex items-center justify-between">
                                        <h5 className="text-xs font-bold font-mono text-slate-200">{milestone.title}</h5>
                                        <span className={`text-[8px] font-mono font-bold px-2 py-0.5 rounded border ${
                                            milestone.state === 'COMPLETED' ? 'bg-emerald-950/40 text-emerald-400 border-emerald-500/20' :
                                            milestone.state === 'IN PROGRESS' ? 'bg-indigo-950/40 text-indigo-400 border-indigo-500/20' : 'bg-slate-900 text-slate-500 border-slate-800'
                                        }`}>{milestone.state}</span>
                                    </div>
                                    <p className="text-[11px] text-slate-400 leading-normal">{milestone.desc}</p>
                                    <div className="flex items-center gap-3 pt-2">
                                        <div className="flex-grow bg-slate-900 h-1 rounded-full overflow-hidden">
                                            <div className="bg-cyan-400 h-full shadow-[0_0_8px_#22d3ee]" style={{ width: `${milestone.progress}%` }}></div>
                                        </div>
                                        <span className="text-[10px] font-mono text-slate-500">{milestone.duration}</span>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );

    // Risk Analyzer Screen Component
    const renderRiskAnalyzer = () => {
        const highRiskTasks = tasks.filter(t => t.daysRemaining <= 3 && t.progress < 50);
        return (
            <div className="bg-slate-900 border border-slate-800/80 p-8 rounded-3xl space-y-8 relative overflow-hidden">
                <div className="absolute top-0 right-0 w-64 h-64 bg-rose-500/5 rounded-full blur-3xl pointer-events-none"></div>

                <div className="space-y-2 border-b border-slate-800 pb-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
                    <div className="space-y-1">
                        <span className="text-[10px] font-mono tracking-widest text-rose-400 font-bold uppercase">&gt; CYBERNETIC RISK COEFICIENTS</span>
                        <h3 className="text-2xl font-black font-mono uppercase tracking-wide">Predictive Risk Analyzer</h3>
                        <p className="text-slate-400 text-xs leading-relaxed">
                            Continuous delivery audit metrics checking task progress velocity against target milestones.
                        </p>
                    </div>
                    <div className="bg-rose-950/40 border border-rose-500/20 px-4 py-3 rounded-2xl flex items-center gap-3 self-start md:self-auto">
                        <span className="w-2.5 h-2.5 bg-rose-500 rounded-full animate-ping"></span>
                        <div>
                            <span className="text-[9px] text-rose-400 font-mono font-bold block leading-none">THREAT LEVEL</span>
                            <span className="text-xs font-mono font-black text-rose-300 uppercase">DEFCON 1 HIGH</span>
                        </div>
                    </div>
                </div>

                {/* Risk Metrics Cards */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    {[
                        { label: "Overall Slippage Risk Coefficient", value: "14.8%", color: "text-amber-400", indicator: "STABLE" },
                        { label: "Active Backlog Workload Pressure", value: "3.5 hrs/day", color: "text-rose-400", indicator: "HIGH PRESSURE" },
                        { label: "Predictive Zero-Slippage Probability", value: "91.2%", color: "text-cyan-400", indicator: "EVALUATING" }
                    ].map((m, idx) => (
                        <div key={idx} className="bg-slate-950/60 border border-slate-805 p-5 rounded-2xl space-y-2">
                            <span className="text-[9px] font-mono font-bold text-slate-500 uppercase tracking-wider block">{m.label}</span>
                            <p className={`text-2xl font-black font-mono ${m.color}`}>{m.value}</p>
                            <span className="inline-block text-[8px] font-mono bg-slate-900 border border-slate-800 px-1.5 py-0.5 rounded text-slate-400 uppercase tracking-widest">{m.indicator}</span>
                        </div>
                    ))}
                </div>

                {/* High-Risk Tasks Audit */}
                <div className="space-y-4">
                    <h4 className="text-xs font-bold font-mono tracking-wider text-slate-300 uppercase">HIGH-RISK DEADLINE AUDIT</h4>
                    
                    {highRiskTasks.length === 0 ? (
                        <div className="bg-slate-950/40 border border-slate-850 p-8 rounded-2xl text-center space-y-2">
                            <p className="text-3xl">🛡️</p>
                            <p className="text-xs font-mono text-cyan-400 uppercase font-black">ZERO IMMINENT THREATS DETECTED</p>
                            <p className="text-[10px] text-slate-500 max-w-sm mx-auto font-mono">All active roadmaps are tracking well inside expected delivery parameters.</p>
                        </div>
                    ) : (
                        <div className="space-y-3">
                            {highRiskTasks.map((t) => (
                                <div key={t.id} className="bg-slate-950/60 border border-rose-500/20 p-5 rounded-2xl flex flex-col md:flex-row md:items-center justify-between gap-4">
                                    <div className="space-y-1">
                                        <div className="flex items-center gap-2">
                                            <span className="text-xs font-bold font-mono text-slate-200">{t.title}</span>
                                            <span className="text-[8px] font-mono font-bold bg-rose-950/50 text-rose-400 border border-rose-500/20 px-2 py-0.5 rounded uppercase">IMMINENT SLIPPAGE</span>
                                        </div>
                                        <p className="text-[11px] text-slate-400">{t.description}</p>
                                    </div>
                                    <div className="flex items-center gap-6">
                                        <div className="text-right">
                                            <span className="text-[9px] text-slate-500 font-mono block">DAYS REMAINING</span>
                                            <span className="text-xs font-bold font-mono text-rose-400">{t.daysRemaining} days</span>
                                        </div>
                                        <div className="text-right">
                                            <span className="text-[9px] text-slate-500 font-mono block">CURRENT PROGRESS</span>
                                            <span className="text-xs font-bold font-mono text-slate-300">{t.progress}%</span>
                                        </div>
                                        <button
                                            onClick={() => alert("Simulation: AI Emergency Mode Pomodoro sprint scheduled.")}
                                            className="px-4 py-2.5 bg-rose-500 hover:bg-rose-600 text-slate-950 font-mono font-bold text-[10px] tracking-wider uppercase rounded-xl transition-all"
                                        >
                                            🚨 INITIATE EMERGENCY SPRINT
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        );
    };

    // Task History Component
    const renderTaskHistory = () => {
        const completedTasks = tasks.filter(t => t.progress === 100);
        return (
            <div className="bg-slate-900 border border-slate-800/80 p-8 rounded-3xl space-y-8 relative overflow-hidden">
                <div className="absolute top-0 right-0 w-64 h-64 bg-indigo-500/5 rounded-full blur-3xl pointer-events-none"></div>

                <div className="space-y-2 border-b border-slate-800 pb-6">
                    <span className="text-[10px] font-mono tracking-widest text-indigo-400 font-bold uppercase">&gt; ARCHIVED AUDIT RECORDS</span>
                    <h3 className="text-2xl font-black font-mono uppercase tracking-wide">Secure Task History Log</h3>
                    <p className="text-slate-400 text-xs leading-relaxed">
                        Historically completed or terminated roadmaps with focus performance audit logs.
                    </p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                    {[
                        { label: "Total Completed Roadmaps", value: completedTasks.length },
                        { label: "Manual Terminations", value: "3" },
                        { label: "Total Saved Focus Hours", value: `${completedTasks.length * 6} hrs` },
                        { label: "Success Coefficient", value: "92.3%" }
                    ].map((stat, idx) => (
                        <div key={idx} className="bg-slate-950/40 border border-slate-850 p-4 rounded-xl text-center">
                            <span className="text-[9px] font-mono font-bold text-slate-500 uppercase tracking-wider block">{stat.label}</span>
                            <span className="text-xl font-mono font-black text-cyan-400 block mt-1">{stat.value}</span>
                        </div>
                    ))}
                </div>

                <div className="space-y-4">
                    <h4 className="text-xs font-bold font-mono tracking-wider text-slate-300 uppercase">SECURE TIMELINE TRANSCRIPT</h4>
                    
                    {completedTasks.length === 0 ? (
                        <div className="bg-slate-950/40 border border-slate-850 p-8 rounded-2xl text-center space-y-2">
                            <p className="text-3xl">📜</p>
                            <p className="text-xs font-mono text-indigo-400 uppercase font-black">NO COMPLETED ROADMAP TIMELINES FOUND</p>
                            <p className="text-[10px] text-slate-500 max-w-sm mx-auto font-mono">Complete milestones or subtasks inside your Dashboard to archive logs securely.</p>
                        </div>
                    ) : (
                        <div className="space-y-3">
                            {completedTasks.map((t) => (
                                <div key={t.id} className="bg-slate-950/50 border border-slate-850 p-5 rounded-2xl flex items-center justify-between">
                                    <div className="space-y-1">
                                        <h5 className="text-xs font-bold font-mono text-slate-200">{t.title}</h5>
                                        <p className="text-[10px] text-slate-500 font-mono">ID: {t.id} &bull; Category: {t.category}</p>
                                    </div>
                                    <span className="text-[9px] font-mono font-bold bg-emerald-950/50 text-emerald-400 border border-emerald-500/20 px-2.5 py-1 rounded-xl uppercase">
                                        ✓ SECURED
                                    </span>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        );
    };

    // Keep activeView state and activeTab sidebar synchronized
    useEffect(() => {
        if (activeView === 'calendar') {
            setActiveTab('calendar');
        } else if (activeView === 'dashboard') {
            setActiveTab('dashboard');
        }
    }, [activeView]);

    return (
        <AppLayout
            activeTab={activeTab}
            onTabChange={(tab) => {
                setActiveTab(tab);
                if (tab === 'calendar') {
                    setActiveView('calendar');
                } else {
                    setActiveView('dashboard');
                }
            }}
            onLogout={handleLogout}
            notifications={broadcastNotifications}
        >
            <AnimatePresence mode="wait">
                {activeTab === 'dashboard' && (
                    <motion.div
                        key="dashboard"
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -10 }}
                        className="space-y-8"
                    >
                        {/* Task Dashboard Main View Header */}
                        <div className="flex flex-col md:flex-row justify-between items-start md:items-center border-b border-slate-900 pb-6 gap-4">
                            <div>
                                <div className="flex items-center gap-2 mb-1">
                                    <span className="w-2 h-2 rounded-full bg-cyan-400 animate-pulse"></span>
                                    <span className="text-[10px] font-bold font-mono tracking-widest text-cyan-400 uppercase">Autonomous Agent Online</span>
                                </div>
                                <h1 className="text-2xl font-black font-mono uppercase text-slate-100">Task Workspace Dashboard</h1>
                                <p className="text-slate-400 text-xs">Manage active subtask roadmaps, strategic categories, and priority metrics.</p>
                            </div>
                            <div className="flex items-center gap-3">
                                <button
                                    onClick={() => setShowOnboarding(true)}
                                    className="flex items-center gap-2 px-4 py-2.5 bg-slate-900/60 hover:bg-slate-850/80 border border-slate-800 hover:border-slate-700 active:bg-slate-950 text-xs font-bold font-mono text-cyan-400 hover:text-cyan-300 tracking-wider uppercase rounded-xl transition-all shadow-md"
                                >
                                    💡 REPLAY GUIDE
                                </button>
                            </div>
                        </div>

                        {activeView === 'calendar' ? (
                            <CalendarView userId="premium-web-user" onBack={() => setActiveView('dashboard')} />
                        ) : (
                            /* Dashboard Main Grid */
                            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
                    
                    {/* Left Column (Forms & Control Panel) - Span 5 */}
                    <div className="lg:col-span-5 space-y-8">
                        
                        {/* Voice Assistant Module Card */}
                        <div className="bg-slate-900 border border-slate-800 rounded-3xl overflow-hidden p-0.5">
                            <VoiceAssistant userId="premium-web-user" />
                        </div>

                        {/* Task Addition Form Card */}
                        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl relative">
                            <div className="absolute top-0 right-10 w-24 h-24 bg-emerald-500/5 rounded-full filter blur-2xl pointer-events-none"></div>
                            
                            <h2 className="text-xl font-bold mb-6 text-slate-100 flex items-center gap-2 font-mono">
                                <span className="text-emerald-400">&gt;</span> CREATE GUARDIAN TASK
                            </h2>

                            <form onSubmit={handleAddTask} className="space-y-4">
                                <div>
                                    <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5">Task Name / Objective</label>
                                    <input 
                                        type="text"
                                        required
                                        value={title}
                                        onChange={(e) => setTitle(e.target.value)}
                                        placeholder="e.g. Study for exam, submit project"
                                        className="w-full bg-slate-950 border border-slate-800 focus:border-cyan-500 rounded-xl px-4 py-3 text-sm text-slate-100 focus:outline-none transition-all placeholder:text-slate-600"
                                    />
                                </div>

                                <div>
                                    <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5">Description / Details</label>
                                    <textarea 
                                        value={description}
                                        onChange={(e) => setDescription(e.target.value)}
                                        placeholder="Explain core deliverables or specific objectives..."
                                        rows="2"
                                        className="w-full bg-slate-950 border border-slate-800 focus:border-cyan-500 rounded-xl px-4 py-2.5 text-sm text-slate-100 focus:outline-none transition-all placeholder:text-slate-600 resize-none"
                                    />
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5">Category</label>
                                        <select 
                                            value={category}
                                            onChange={(e) => setCategory(e.target.value)}
                                            className="w-full bg-slate-950 border border-slate-800 focus:border-cyan-500 rounded-xl px-3 py-3 text-sm text-slate-100 focus:outline-none transition-all"
                                        >
                                            <option value="Assignment">Assignment</option>
                                            <option value="Exam">Exam</option>
                                            <option value="Interview">Interview</option>
                                            <option value="Project">Project</option>
                                            <option value="Bill payment">Bill payment</option>
                                            <option value="Lab">Lab</option>
                                            <option value="Presentation">Presentation</option>
                                            <option value="Other">Other</option>
                                        </select>
                                    </div>

                                    <div>
                                        <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5">Days Remaining</label>
                                        <input 
                                            type="number"
                                            required
                                            min="1"
                                            value={daysRemaining}
                                            onChange={(e) => setDaysRemaining(e.target.value)}
                                            placeholder="e.g. 5"
                                            className="w-full bg-slate-950 border border-slate-800 focus:border-cyan-500 rounded-xl px-4 py-3 text-sm text-slate-100 focus:outline-none transition-all placeholder:text-slate-600"
                                        />
                                    </div>
                                </div>

                                {/* Smart Prioritization Factors */}
                                <div className="grid grid-cols-2 gap-4 bg-slate-950/40 p-3.5 border border-slate-850 rounded-2xl">
                                    <div>
                                        <div className="flex justify-between items-center mb-1.5">
                                            <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider">Difficulty</label>
                                            <span className="text-xs font-bold font-mono text-amber-400">{difficulty}/5</span>
                                        </div>
                                        <input 
                                            type="range"
                                            min="1"
                                            max="5"
                                            value={difficulty}
                                            onChange={(e) => setDifficulty(parseInt(e.target.value))}
                                            className="w-full h-1.5 bg-slate-950 rounded-lg appearance-none cursor-pointer accent-cyan-500 border border-slate-850"
                                        />
                                    </div>

                                    <div>
                                        <div className="flex justify-between items-center mb-1.5">
                                            <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider">Importance</label>
                                            <span className="text-xs font-bold font-mono text-cyan-400">{importanceLevel}/5</span>
                                        </div>
                                        <input 
                                            type="range"
                                            min="1"
                                            max="5"
                                            value={importanceLevel}
                                            onChange={(e) => setImportanceLevel(parseInt(e.target.value))}
                                            className="w-full h-1.5 bg-slate-950 rounded-lg appearance-none cursor-pointer accent-cyan-500 border border-slate-850"
                                        />
                                    </div>
                                </div>

                                <div>
                                    <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5">Estimated Hours Required</label>
                                    <input 
                                        type="number"
                                        min="0.5"
                                        step="0.5"
                                        value={estimatedHours}
                                        onChange={(e) => setEstimatedHours(e.target.value)}
                                        placeholder="e.g. 12"
                                        className="w-full bg-slate-950 border border-slate-800 focus:border-cyan-500 rounded-xl px-4 py-3 text-sm text-slate-100 focus:outline-none transition-all placeholder:text-slate-600"
                                    />
                                </div>

                                {/* Priority Selector */}
                                <div>
                                    <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Priority Level</label>
                                    <div className="grid grid-cols-4 gap-1.5">
                                        {['AUTO', 'HIGH', 'MEDIUM', 'LOW'].map((prio) => {
                                            const isActive = priority === prio;
                                            const colorClass = 
                                                prio === 'AUTO' ? (isActive ? 'bg-cyan-500/10 border-cyan-500 text-cyan-400 animate-pulse' : 'bg-slate-950 border-slate-800 text-slate-500') :
                                                prio === 'HIGH' ? (isActive ? 'bg-rose-500/10 border-rose-500 text-rose-400' : 'bg-slate-950 border-slate-800 text-slate-500') :
                                                prio === 'MEDIUM' ? (isActive ? 'bg-amber-500/10 border-amber-500 text-amber-400' : 'bg-slate-950 border-slate-800 text-slate-500') :
                                                (isActive ? 'bg-emerald-500/10 border-emerald-500 text-emerald-400' : 'bg-slate-950 border-slate-800 text-slate-500');

                                            return (
                                                <button
                                                    key={prio}
                                                    type="button"
                                                    onClick={() => setPriority(prio)}
                                                    className={`border rounded-xl py-2 text-[11px] font-bold font-mono tracking-wide transition-all ${colorClass}`}
                                                >
                                                    {prio}
                                                </button>
                                            );
                                        })}
                                    </div>
                                </div>

                                {/* Roadmap Option Toggle */}
                                <div className="pt-2">
                                    <label className="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">Roadmap Strategy</label>
                                    <div className="grid grid-cols-2 gap-3">
                                        <button
                                            type="button"
                                            onClick={() => setRoadmapType('AI')}
                                            className={`flex items-center justify-center gap-2 border rounded-xl py-2.5 text-xs font-bold font-mono transition-all ${
                                                roadmapType === 'AI' 
                                                    ? 'bg-cyan-500/15 border-cyan-500 text-cyan-400' 
                                                    : 'bg-slate-950 border-slate-800 text-slate-500'
                                            }`}
                                        >
                                            <span className="text-[10px]">⚡</span> AI GENERATED
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => setRoadmapType('MANUAL')}
                                            className={`flex items-center justify-center gap-2 border rounded-xl py-2.5 text-xs font-bold font-mono transition-all ${
                                                roadmapType === 'MANUAL' 
                                                    ? 'bg-emerald-500/15 border-emerald-500 text-emerald-400' 
                                                    : 'bg-slate-950 border-slate-800 text-slate-500'
                                            }`}
                                        >
                                            <span>✍️</span> MANUAL ROADMAP
                                        </button>
                                    </div>
                                </div>

                                {/* Manual Roadmap Builder */}
                                {roadmapType === 'MANUAL' && (
                                    <div className="bg-slate-950 border border-slate-800/80 rounded-2xl p-4 mt-2 space-y-3">
                                        <h3 className="text-xs font-extrabold text-emerald-400 tracking-wider font-mono">SUBTASK ROADMAP BUILDER</h3>
                                        
                                        <div>
                                            <input 
                                                type="text"
                                                value={subtaskTitle}
                                                onChange={(e) => setSubtaskTitle(e.target.value)}
                                                placeholder="Subtask objective (e.g. Write review notes)"
                                                className="w-full bg-slate-900 border border-slate-800 focus:border-emerald-500 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none placeholder:text-slate-600"
                                            />
                                        </div>

                                        <div className="grid grid-cols-2 gap-2">
                                            <input 
                                                type="number"
                                                value={subtaskDuration}
                                                onChange={(e) => setSubtaskDuration(e.target.value)}
                                                placeholder="Duration (mins)"
                                                className="w-full bg-slate-900 border border-slate-800 focus:border-emerald-500 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none placeholder:text-slate-600"
                                            />
                                            <input 
                                                type="text"
                                                value={subtaskSchedule}
                                                onChange={(e) => setSubtaskSchedule(e.target.value)}
                                                placeholder="Schedule (e.g. Day 1)"
                                                className="w-full bg-slate-900 border border-slate-800 focus:border-emerald-500 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none placeholder:text-slate-600"
                                            />
                                        </div>

                                        <button
                                            type="button"
                                            onClick={handleAddTempSubtask}
                                            className="w-full bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-bold font-mono text-xs py-2 rounded-lg transition-all"
                                        >
                                            + ADD ROADMAP ITEM
                                        </button>

                                        {/* Added Temporary Subtasks list preview */}
                                        {tempSubtasks.length > 0 && (
                                            <div className="space-y-1.5 pt-2 border-t border-slate-900">
                                                <p className="text-[10px] font-bold text-slate-500 uppercase">Items to include ({tempSubtasks.length}):</p>
                                                {tempSubtasks.map((sub) => (
                                                    <div key={sub.id} className="flex justify-between items-center bg-slate-900/60 px-2 py-1.5 rounded border border-slate-800/50">
                                                        <div className="flex-1 min-w-0 pr-2">
                                                            <p className="text-xs text-slate-300 font-semibold truncate">{sub.title}</p>
                                                            <p className="text-[10px] text-slate-500">{sub.duration}m • {sub.scheduled}</p>
                                                        </div>
                                                        <button 
                                                            type="button"
                                                            onClick={() => handleRemoveTempSubtask(sub.id)}
                                                            className="text-rose-500 hover:text-rose-400 text-xs font-bold px-1"
                                                        >
                                                            ✕
                                                        </button>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                )}

                                <button
                                    type="submit"
                                    className="w-full mt-4 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-bold font-mono py-3.5 rounded-xl shadow-lg shadow-cyan-500/10 transition-all text-sm tracking-widest"
                                >
                                    DEPLOY GUARDIAN SHIELD
                                </button>
                            </form>
                        </div>
                    </div>

                    {/* Right Column (Active Tasks & Progress) - Span 7 */}
                    <div className="lg:col-span-7 space-y-6">
                        
                        {/* Summary Metrics */}
                        <div className="grid grid-cols-3 gap-4">
                            <div className="bg-slate-900/40 border border-slate-800/80 rounded-2xl p-4 flex flex-col justify-between">
                                <span className="text-slate-500 text-xs font-mono font-bold tracking-wider uppercase">Active Tasks</span>
                                <span className="text-2xl font-black text-white mt-1">{tasks.length}</span>
                            </div>
                            <div className="bg-slate-900/40 border border-slate-800/80 rounded-2xl p-4 flex flex-col justify-between">
                                <span className="text-slate-500 text-xs font-mono font-bold tracking-wider uppercase">High Priority</span>
                                <span className="text-2xl font-black text-rose-500 mt-1">
                                    {tasks.filter(t => t.priority === 'HIGH').length}
                                </span>
                            </div>
                            <div className="bg-slate-900/40 border border-slate-800/80 rounded-2xl p-4 flex flex-col justify-between">
                                <span className="text-slate-500 text-xs font-mono font-bold tracking-wider uppercase">Completed</span>
                                <span className="text-2xl font-black text-emerald-400 mt-1">
                                    {tasks.filter(t => t.progress === 100).length}
                                </span>
                            </div>
                        </div>

                        {/* Control Deck for Analysis and Notifications */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {/* Toggle Analysis Button */}
                            <div className="flex flex-col justify-between bg-slate-900 border border-slate-800 rounded-3xl p-5 gap-4">
                                <div>
                                    <h3 className="text-sm font-bold text-slate-100 flex items-center gap-1.5 font-mono">
                                        <span className="text-cyan-400">📊</span> ANALYTICS SUITE
                                    </h3>
                                    <p className="text-[11px] text-slate-400 mt-0.5">
                                        Visualize priority distributions, progress completion, and category ratios.
                                    </p>
                                </div>
                                <button
                                    type="button"
                                    onClick={() => setShowAnalysis(!showAnalysis)}
                                    className={`w-full px-4 py-2.5 rounded-xl text-xs font-bold font-mono tracking-wider border transition-all flex items-center justify-center gap-2 ${
                                        showAnalysis 
                                            ? 'bg-rose-500/10 border-rose-500/30 text-rose-400 hover:bg-rose-500/20 shadow-[0_0_15px_rgba(239,68,68,0.07)]' 
                                            : 'bg-cyan-500/10 border-cyan-500/30 text-cyan-400 hover:bg-cyan-500/20 shadow-[0_0_15px_rgba(56,189,248,0.07)]'
                                    }`}
                                >
                                    <span>{showAnalysis ? '✕ HIDE ANALYSIS' : '⚡ VIEW ANALYSIS'}</span>
                                </button>
                            </div>

                            {/* Browser Notifications Controller */}
                            <div className="flex flex-col justify-between bg-slate-900 border border-slate-800 rounded-3xl p-5 gap-4">
                                <div>
                                    <h3 className="text-sm font-bold text-slate-100 flex items-center gap-1.5 font-mono">
                                        <span className={notificationsEnabled ? 'text-emerald-400 animate-bounce' : 'text-slate-500'}>🔔</span> DEADLINE ENGINE
                                    </h3>
                                    <p className="text-[11px] text-slate-400 mt-0.5">
                                        Enable high-priority browser alerts for tasks approaching their deadline within 24 hours.
                                    </p>
                                </div>
                                <button
                                    type="button"
                                    onClick={handleToggleNotifications}
                                    className={`w-full px-4 py-2.5 rounded-xl text-xs font-bold font-mono tracking-wider border transition-all flex items-center justify-center gap-2 ${
                                        notificationsEnabled 
                                            ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400 hover:bg-emerald-500/20 shadow-[0_0_15px_rgba(16,185,129,0.07)]' 
                                            : 'bg-slate-800/60 border-slate-700 text-slate-300 hover:bg-slate-800 hover:text-white'
                                    }`}
                                >
                                    <span>{notificationsEnabled ? '🔔 ALERTS ACTIVE' : '🔕 ALERTS INACTIVE'}</span>
                                </button>
                            </div>
                        </div>

                        {/* Recharts Analytics Panel */}
                        {showAnalysis && (
                            <div className="bg-slate-900/60 border border-slate-800/80 rounded-3xl p-6 shadow-xl relative overflow-hidden transition-all duration-300">
                                <div className="absolute top-0 right-0 w-32 h-32 bg-cyan-500/5 rounded-full filter blur-3xl pointer-events-none"></div>
                                <div className="flex items-center justify-between mb-6">
                                    <h3 className="text-sm font-extrabold text-slate-300 uppercase tracking-widest font-mono flex items-center gap-2">
                                        <span className="text-cyan-400">⚡</span> GUARDIAN TASK ANALYTICS
                                    </h3>
                                    <span className="text-[10px] bg-cyan-500/10 text-cyan-400 font-mono font-semibold px-2 py-0.5 rounded border border-cyan-500/20">LIVE DATA</span>
                                </div>
                                
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    {/* Priority and Completion Stacked Bar Chart */}
                                    <div className="bg-slate-950/50 border border-slate-800/60 rounded-2xl p-4 flex flex-col h-[280px]">
                                        <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 font-mono">Priority & Status distribution</h4>
                                        <div className="flex-1 w-full min-h-[180px]">
                                            <ResponsiveContainer width="100%" height="100%">
                                                <BarChart data={priorityData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                                                    <XAxis dataKey="name" stroke="#64748b" fontSize={10} tickLine={false} />
                                                    <YAxis stroke="#64748b" fontSize={10} tickLine={false} allowDecimals={false} />
                                                    <Tooltip 
                                                        contentStyle={{ backgroundColor: '#0f172a', border: '1px solid #334155', borderRadius: '12px' }}
                                                        itemStyle={{ fontSize: '11px', color: '#f1f5f9' }}
                                                        labelStyle={{ fontSize: '11px', fontWeight: 'bold', color: '#38bdf8' }}
                                                    />
                                                    <Legend verticalAlign="top" height={36} iconType="circle" iconSize={8} wrapperStyle={{ fontSize: '10px', color: '#94a3b8' }} />
                                                    <Bar dataKey="Completed" stackId="a" fill="#10b981" radius={[0, 0, 0, 0]} />
                                                    <Bar dataKey="Pending" stackId="a" fill="#38bdf8" radius={[4, 4, 0, 0]} />
                                                </BarChart>
                                            </ResponsiveContainer>
                                        </div>
                                    </div>

                                    {/* Category breakdown pie chart */}
                                    <div className="bg-slate-950/50 border border-slate-800/60 rounded-2xl p-4 flex flex-col h-[280px]">
                                        <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 font-mono">Completion Rate Summary</h4>
                                        <div className="flex-1 w-full flex items-center justify-center min-h-[180px]">
                                            <div className="relative w-full h-full flex items-center justify-center">
                                                <ResponsiveContainer width="100%" height="100%">
                                                    <PieChart>
                                                        <Pie
                                                            data={statusPieData}
                                                            cx="50%"
                                                            cy="50%"
                                                            innerRadius={55}
                                                            outerRadius={75}
                                                            paddingAngle={4}
                                                            dataKey="value"
                                                        >
                                                            {statusPieData.map((entry, index) => (
                                                                <Cell key={`cell-${index}`} fill={entry.color} />
                                                            ))}
                                                        </Pie>
                                                        <Tooltip
                                                            contentStyle={{ backgroundColor: '#0f172a', border: '1px solid #334155', borderRadius: '12px' }}
                                                            itemStyle={{ fontSize: '11px', color: '#f1f5f9' }}
                                                        />
                                                    </PieChart>
                                                </ResponsiveContainer>
                                                <div className="absolute flex flex-col items-center justify-center pointer-events-none">
                                                    <span className="text-xl font-extrabold text-white">{overallCompletionPercent}%</span>
                                                    <span className="text-[9px] text-slate-500 uppercase tracking-wider font-mono">Done</span>
                                                </div>
                                            </div>
                                        </div>
                                        {/* Pie Chart Custom Legend */}
                                        <div className="flex justify-center gap-4 mt-2">
                                            {statusPieData.map((item, index) => (
                                                <div key={index} className="flex items-center gap-1.5">
                                                    <span className="w-2 h-2 rounded-full" style={{ backgroundColor: item.color }}></span>
                                                    <span className="text-[10px] text-slate-400 font-mono">{item.name} ({item.value})</span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* Task List Header */}
                        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                            <h3 className="text-sm font-extrabold text-slate-300 uppercase tracking-widest font-mono">
                                ACTIVE GUARDIAN ROADMAPS
                            </h3>
                            <span className="text-xs text-slate-500 font-mono">Sorted by proximity</span>
                        </div>

                        {/* Task Cards Stack */}
                        {tasks.length === 0 ? (
                            <div className="bg-slate-900/10 border-2 border-dashed border-slate-800 rounded-3xl p-12 text-center">
                                <div className="text-3xl mb-3">🛡️</div>
                                <h4 className="font-bold text-slate-300 text-base">Your Guardian Shield is Empty</h4>
                                <p className="text-slate-500 text-xs mt-1 max-w-xs mx-auto">
                                    Create a new task manually or click the voice assistant microphone to transcribe a custom command.
                                </p>
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {tasks.map((task) => {
                                    // Identify priority colors
                                    const isHigh = task.priority === 'HIGH';
                                    const isMedium = task.priority === 'MEDIUM';
                                    const priorityBadgeColor = 
                                        isHigh ? 'bg-rose-500/10 border-rose-500/30 text-rose-400' :
                                        isMedium ? 'bg-amber-500/10 border-amber-500/30 text-amber-400' :
                                        'bg-emerald-500/10 border-emerald-500/30 text-emerald-400';

                                    const isCompleted = task.progress === 100;

                                    return (
                                        <div 
                                            key={task.id} 
                                            className={`bg-slate-900 border transition-all rounded-3xl overflow-hidden shadow-lg ${
                                                isCompleted 
                                                    ? 'border-emerald-500/20 opacity-80' 
                                                    : isHigh 
                                                    ? 'border-rose-500/20 hover:border-rose-500/40' 
                                                    : 'border-slate-800 hover:border-slate-700'
                                            }`}
                                        >
                                            {/* Card Top Block */}
                                            <div className="p-6">
                                                <div className="flex flex-wrap items-center justify-between gap-2 mb-3">
                                                    {/* Category & Strategy Tag */}
                                                    <div className="flex items-center gap-1.5 flex-wrap">
                                                        <span className="px-2 py-0.5 bg-slate-950 border border-slate-800 text-[10px] text-slate-400 font-bold tracking-wider rounded uppercase">
                                                            {task.category}
                                                        </span>
                                                        <span className={`px-2 py-0.5 border text-[10px] font-bold tracking-wider rounded uppercase ${
                                                            task.roadmapType === 'AI' 
                                                                ? 'bg-cyan-950/40 border-cyan-500/20 text-cyan-400' 
                                                                : 'bg-emerald-950/40 border-emerald-500/20 text-emerald-400'
                                                        }`}>
                                                            {task.roadmapType === 'AI' ? '⚡ AI Roadmap' : '✍️ Manual'}
                                                        </span>
                                                        {task.difficulty !== undefined && (
                                                            <span className="px-2 py-0.5 bg-slate-950 border border-slate-800/80 text-[10px] text-amber-400 font-mono font-bold rounded">
                                                                💪 DIFF: {task.difficulty}/5
                                                            </span>
                                                        )}
                                                        {task.importanceLevel !== undefined && (
                                                            <span className="px-2 py-0.5 bg-slate-950 border border-slate-800/80 text-[10px] text-cyan-400 font-mono font-bold rounded">
                                                                ⭐ IMP: {task.importanceLevel}/5
                                                            </span>
                                                        )}
                                                        {task.estimatedHours !== undefined && (
                                                            <span className="px-2 py-0.5 bg-slate-950 border border-slate-800/80 text-[10px] text-indigo-400 font-mono font-bold rounded">
                                                                ⏱️ {task.estimatedHours} HRS
                                                             </span>
                                                         )}
                                                     </div>

                                                    {/* Priority Badge & Status Badge with Framer Motion */}
                                                    <div className="flex items-center gap-1.5">
                                                        <span className={`px-2.5 py-0.5 border text-[10px] font-bold font-mono tracking-widest rounded-full uppercase ${priorityBadgeColor}`}>
                                                            {task.priority}
                                                        </span>
                                                        <AnimatePresence mode="wait">
                                                            <motion.span
                                                                key={isCompleted ? 'completed' : task.progress > 0 ? 'inprogress' : 'pending'}
                                                                initial={{ opacity: 0, scale: 0.8, y: -4 }}
                                                                animate={{ opacity: 1, scale: 1, y: 0 }}
                                                                exit={{ opacity: 0, scale: 0.8, y: 4 }}
                                                                transition={{ type: "spring", stiffness: 400, damping: 25 }}
                                                                className={`px-2.5 py-0.5 border text-[10px] font-bold font-mono tracking-widest rounded-full uppercase ${
                                                                    isCompleted 
                                                                        ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400 shadow-[0_0_12px_rgba(16,185,129,0.06)]' 
                                                                        : task.progress > 0 
                                                                        ? 'bg-indigo-500/10 border-indigo-500/30 text-indigo-400 shadow-[0_0_12px_rgba(99,102,241,0.06)]' 
                                                                        : 'bg-slate-500/10 border-slate-500/30 text-slate-400'
                                                                }`}
                                                            >
                                                                {isCompleted ? '✓ Done' : task.progress > 0 ? '⚡ In Progress' : '⏳ Pending'}
                                                            </motion.span>
                                                        </AnimatePresence>
                                                    </div>
                                                </div>

                                                {/* Title & Description */}
                                                <div className="mb-4">
                                                    <h4 className={`text-lg font-bold leading-snug ${isCompleted ? 'text-slate-500 line-through' : 'text-slate-100'}`}>
                                                        {task.title}
                                                    </h4>
                                                    {task.description && (
                                                        <p className="text-xs text-slate-400 leading-relaxed mt-1">
                                                            {task.description}
                                                        </p>
                                                    )}
                                                </div>

                                                {/* Metrics and Deadline Alert Row */}
                                                <div className="flex items-center justify-between bg-slate-950 border border-slate-800/60 rounded-2xl p-3 mb-4">
                                                    <div className="flex items-center gap-2">
                                                        <span className="text-xs text-slate-500 font-mono">Countdown:</span>
                                                        <span className={`text-xs font-bold font-mono px-2 py-0.5 rounded ${
                                                            task.daysRemaining <= 2 
                                                                ? 'bg-rose-500/20 text-rose-400 animate-pulse' 
                                                                : 'bg-slate-900 text-slate-300'
                                                        }`}>
                                                            <span className="inline-flex items-center gap-1.5 font-sans">
                                                                 <button 
                                                                     type="button"
                                                                     onClick={() => handleUpdateDaysRemaining(task.id, -1)}
                                                                     className="w-4 h-4 flex items-center justify-center text-xs font-black text-rose-400 hover:text-rose-200 active:scale-90 transition-all cursor-pointer select-none"
                                                                     title="Decrease Days Left"
                                                                 >
                                                                     -
                                                                 </button>
                                                                 <span className="font-mono text-[11px] font-bold px-1">{task.daysRemaining} days left</span>
                                                                 <button 
                                                                     type="button"
                                                                     onClick={() => handleUpdateDaysRemaining(task.id, 1)}
                                                                     className="w-4 h-4 flex items-center justify-center text-xs font-black text-cyan-400 hover:text-cyan-200 active:scale-90 transition-all cursor-pointer select-none"
                                                                     title="Increase Days Left"
                                                                 >
                                                                     +
                                                                 </button>
                                                             </span>
                                                        </span>
                                                    </div>

                                                    <div className="flex items-center gap-2">
                                                        <span className="text-xs text-slate-500 font-mono">Progress:</span>
                                                        <span className="text-xs font-bold font-mono text-cyan-400">{task.progress}%</span>
                                                    </div>
                                                </div>

                                                {/* Progress Bar */}
                                                <div className="w-full bg-slate-950 h-1.5 rounded-full overflow-hidden mb-5">
                                                    <div 
                                                        className={`h-full transition-all duration-500 ${
                                                            isCompleted ? 'bg-emerald-500' : 'bg-gradient-to-r from-cyan-500 to-blue-500'
                                                        }`}
                                                        style={{ width: `${task.progress}%` }}
                                                    ></div>
                                                </div>

                                                {/* Roadmap Checklist Subtasks */}
                                                {task.subtasks && task.subtasks.length > 0 && (
                                                    <div className="space-y-2 border-t border-slate-800/80 pt-4">
                                                        <p className="text-[10px] font-bold font-mono tracking-widest text-slate-500 uppercase mb-3">
                                                            Active Roadmap Execution Plan
                                                        </p>
                                                        <div className="space-y-1.5">
                                                            {task.subtasks.map((sub) => {
                                                                const isSubDone = sub.status === 'COMPLETED';
                                                                return (
                                                                    <div 
                                                                        key={sub.id}
                                                                        onClick={() => handleToggleSubtask(task.id, sub.id)}
                                                                        className={`flex items-center justify-between p-2.5 rounded-xl border transition-all cursor-pointer select-none ${
                                                                            isSubDone 
                                                                                ? 'bg-emerald-950/5 border-emerald-500/10 text-slate-500' 
                                                                                : 'bg-slate-950/60 border-slate-800 hover:border-slate-700 text-slate-300'
                                                                        }`}
                                                                    >
                                                                        <div className="flex items-center gap-3 min-w-0 pr-4">
                                                                            {/* Checkbox */}
                                                                            <span className={`w-4 h-4 rounded flex items-center justify-center border transition-all ${
                                                                                isSubDone 
                                                                                    ? 'bg-emerald-500 border-emerald-400 text-slate-950' 
                                                                                    : 'border-slate-700 hover:border-cyan-500'
                                                                            }`}>
                                                                                {isSubDone && <span className="text-[10px] font-black">✓</span>}
                                                                            </span>
                                                                            <span className={`text-xs font-semibold truncate ${isSubDone ? 'line-through text-slate-500' : ''}`}>
                                                                                {sub.title}
                                                                            </span>
                                                                        </div>

                                                                        <div className="flex items-center gap-2 flex-shrink-0">
                                                                            <span className="text-[10px] font-mono bg-slate-900 border border-slate-800/80 px-1.5 py-0.5 rounded text-slate-400">
                                                                                {sub.duration} min
                                                                            </span>
                                                                            <span className="text-[10px] font-mono text-cyan-400 font-semibold uppercase">
                                                                                {sub.scheduled}
                                                                            </span>
                                                                        </div>
                                                                    </div>
                                                                );
                                                            })}
                                                        </div>
                                                    </div>
                                                )}
                                            </div>

                                            {/* Card Bottom Panel Actions */}
                                            <div className="bg-slate-950/40 border-t border-slate-800/50 px-6 py-3.5 flex justify-between items-center gap-4 flex-wrap">
                                                <span className="text-[10px] text-slate-500 font-mono">ID: {task.id}</span>
                                                <div className="flex gap-4">
                                                    <button 
                                                        onClick={async () => {
                                                            try {
                                                                const res = await fetch('/api/calendar/schedule-task', {
                                                                    method: 'POST',
                                                                    headers: { 'Content-Type': 'application/json' },
                                                                    body: JSON.stringify({ userId: 'premium-web-user', taskId: task.id, subtasks: task.subtasks })
                                                                });
                                                                if (res.ok) {
                                                                    setActiveView('calendar');
                                                                } else {
                                                                    throw new Error();
                                                                }
                                                            } catch (err) {
                                                                setActiveView('calendar');
                                                            }
                                                        }}
                                                        className="text-xs font-bold text-cyan-400 hover:text-cyan-300 flex items-center gap-1 font-mono transition-all uppercase"
                                                    >
                                                        📅 Schedule On Calendar
                                                    </button>
                                                    <button 
                                                        onClick={() => handleDeleteTask(task.id)}
                                                        className="text-xs font-semibold text-rose-500 hover:text-rose-400 flex items-center gap-1 font-mono transition-all"
                                                    >
                                                        ✕ TERMINATE ROADMAP
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </motion.div>
                )}

                {activeTab === 'planner' && (
                    <motion.div
                        key="planner"
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -10 }}
                    >
                        {renderAIPlanner()}
                    </motion.div>
                )}

                {activeTab === 'calendar' && (
                    <motion.div
                        key="calendar"
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -10 }}
                    >
                        <CalendarView userId="premium-web-user" onBack={() => setActiveTab('dashboard')} />
                    </motion.div>
                )}

                {activeTab === 'analyzer' && (
                    <motion.div
                        key="analyzer"
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -10 }}
                    >
                        {renderRiskAnalyzer()}
                    </motion.div>
                )}

                {activeTab === 'history' && (
                    <motion.div
                        key="history"
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -10 }}
                    >
                        {renderTaskHistory()}
                    </motion.div>
                )}
            </AnimatePresence>

            {/* User Onboarding Guide Overlay */}
            <UserGuideOverlay 
                isOpen={showOnboarding} 
                onClose={() => setShowOnboarding(false)} 
            />
        </AppLayout>
    );
};

export default TaskDashboard;

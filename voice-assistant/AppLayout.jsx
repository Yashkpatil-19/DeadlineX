import React, { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

const AppLayout = ({ children, activeTab, onTabChange, onLogout, notifications = [] }) => {
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);
    const [showProfileMenu, setShowProfileMenu] = useState(false);
    const [showNotifications, setShowNotifications] = useState(false);
    const [user, setUser] = useState({
        name: 'Premium User',
        email: 'user@deadlinex.ai',
        avatar: 'https://api.dicebear.com/7.x/bottts/svg?seed=deadlinex',
        plan: 'Premium Developer Mode',
        joinedAt: new Date().toLocaleDateString()
    });

    const profileRef = useRef(null);
    const notifyRef = useRef(null);

    useEffect(() => {
        const stored = localStorage.getItem('deadlinex_auth_user');
        if (stored) {
            try {
                setUser(JSON.parse(stored));
            } catch (e) {
                console.error("Failed to parse user session", e);
            }
        }
    }, []);

    // Close dropdowns on outside clicks
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (profileRef.current && !profileRef.current.contains(event.target)) {
                setShowProfileMenu(false);
            }
            if (notifyRef.current && !notifyRef.current.contains(event.target)) {
                setShowNotifications(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const sidebarItems = [
        { id: 'dashboard', label: 'Dashboard', icon: '⚡' },
        { id: 'planner', label: 'AI Planner', icon: '🧠' },
        { id: 'calendar', label: 'Calendar View', icon: '📅' },
        { id: 'analyzer', label: 'Risk Analyzer', icon: '📊' },
        { id: 'history', label: 'Task History', icon: '📜' }
    ];

    return (
        <div className="min-h-screen bg-slate-950 text-slate-100 flex font-sans overflow-hidden">
            {/* Cyberpunk Sidebar Navigation */}
            <AnimatePresence mode="wait">
                {isSidebarOpen && (
                    <motion.aside
                        initial={{ width: 0, opacity: 0 }}
                        animate={{ width: 260, opacity: 1 }}
                        exit={{ width: 0, opacity: 0 }}
                        transition={{ duration: 0.3, ease: "easeInOut" }}
                        className="h-screen bg-slate-900/40 border-r border-slate-900 flex flex-col z-40 flex-shrink-0"
                    >
                        {/* Sidebar Brand header */}
                        <div className="p-6 border-b border-slate-900/60 flex items-center justify-between">
                            <div className="flex items-center gap-3">
                                <span className="text-xl">🛡️</span>
                                <div>
                                    <h1 className="text-xs font-black font-mono tracking-widest text-cyan-400">DEADLINEX</h1>
                                    <p className="text-[8px] text-slate-500 font-mono tracking-wider uppercase">Active Security System</p>
                                </div>
                            </div>
                            <button
                                onClick={() => setIsSidebarOpen(false)}
                                className="text-slate-500 hover:text-slate-300 transition-colors md:hidden text-sm"
                            >
                                ✕
                            </button>
                        </div>

                        {/* Sidebar Items */}
                        <nav className="flex-1 px-4 py-6 space-y-2 overflow-y-auto">
                            {sidebarItems.map((item) => {
                                const isActive = activeTab === item.id;
                                return (
                                    <button
                                        key={item.id}
                                        onClick={() => onTabChange(item.id)}
                                        className={`w-full flex items-center gap-3 px-4 py-3 text-xs font-mono font-bold tracking-wider rounded-xl transition-all border ${
                                            isActive
                                                ? 'bg-cyan-500/10 border-cyan-500/30 text-cyan-400 shadow-[0_0_15px_rgba(34,211,238,0.12)]'
                                                : 'bg-transparent border-transparent hover:bg-slate-900/60 hover:border-slate-800 text-slate-400 hover:text-slate-200'
                                        }`}
                                    >
                                        <span className="text-sm">{item.icon}</span>
                                        <span className="uppercase">{item.label}</span>
                                        {isActive && (
                                            <span className="ml-auto w-1.5 h-1.5 bg-cyan-400 rounded-full shadow-[0_0_8px_#22d3ee]"></span>
                                        )}
                                    </button>
                                );
                            })}
                        </nav>

                        {/* Sidebar Footer Info */}
                        <div className="p-6 border-t border-slate-900 bg-slate-950/40 space-y-3">
                            <div className="flex items-center gap-3">
                                <img
                                    src={user.avatar}
                                    alt="User Avatar"
                                    className="w-9 h-9 rounded-xl border border-slate-800 bg-slate-900 p-1"
                                />
                                <div className="truncate">
                                    <p className="text-xs font-mono font-bold truncate text-slate-200">{user.name}</p>
                                    <span className="text-[9px] font-mono font-bold text-rose-400 uppercase tracking-widest">{user.plan}</span>
                                </div>
                            </div>
                        </div>
                    </motion.aside>
                )}
            </AnimatePresence>

            {/* Main Section */}
            <div className="flex-1 flex flex-col h-screen overflow-hidden relative">
                {/* Header Bar */}
                <header className="sticky top-0 z-30 bg-slate-950/80 backdrop-blur-md border-b border-slate-900/80 px-6 py-4 flex items-center justify-between">
                    {/* Collapsible Trigger */}
                    <div className="flex items-center gap-4">
                        <button
                            onClick={() => setIsSidebarOpen(!isSidebarOpen)}
                            className="px-3 py-2 bg-slate-900/60 hover:bg-slate-850/80 border border-slate-800 rounded-xl text-slate-300 transition-all text-xs font-mono font-bold flex items-center gap-1.5"
                        >
                            <span>☰</span> {isSidebarOpen ? 'CLOSE SIDE' : 'OPEN SIDE'}
                        </button>

                        {/* Active Guardian Status Banner */}
                        <div className="hidden md:flex items-center gap-2 px-3 py-1 bg-cyan-950/40 border border-cyan-500/20 rounded-full">
                            <span className="w-1.5 h-1.5 bg-cyan-400 rounded-full animate-pulse shadow-[0_0_8px_#22d3ee]"></span>
                            <span className="text-[9px] font-bold font-mono tracking-widest text-cyan-400 uppercase">
                                STATUS: SECURE (DEFCON 5)
                            </span>
                        </div>
                    </div>

                    {/* Quick Controls */}
                    <div className="flex items-center gap-4">
                        {/* Live Notification Bell with Real-Time Risk Triggers */}
                        <div className="relative" ref={notifyRef}>
                            <button
                                onClick={() => setShowNotifications(!showNotifications)}
                                className="p-2.5 bg-slate-900/60 hover:bg-slate-850 border border-slate-800 rounded-xl text-slate-300 hover:text-cyan-400 transition-all flex items-center justify-center relative"
                            >
                                <span>🔔</span>
                                {notifications.length > 0 && (
                                    <span className="absolute -top-1 -right-1 min-w-4 h-4 px-1 bg-rose-500 text-[8px] font-bold text-slate-100 rounded-full flex items-center justify-center border border-slate-950 shadow-[0_0_8px_rgba(239,68,68,0.4)]">
                                        {notifications.length}
                                    </span>
                                )}
                            </button>

                            <AnimatePresence>
                                {showNotifications && (
                                    <motion.div
                                        initial={{ opacity: 0, y: 10 }}
                                        animate={{ opacity: 1, y: 0 }}
                                        exit={{ opacity: 0, y: 10 }}
                                        className="absolute right-0 mt-3 w-80 bg-slate-900 border border-slate-800 rounded-2xl shadow-[0_10px_30px_rgba(0,0,0,0.5)] overflow-hidden z-50 font-mono"
                                    >
                                        <div className="p-4 border-b border-slate-800 bg-slate-950/50 flex justify-between items-center">
                                            <span className="text-xs font-bold tracking-widest text-slate-300 uppercase">RISK BROADCASTS</span>
                                            <span className="text-[8px] bg-rose-950/50 text-rose-400 border border-rose-500/20 px-1.5 py-0.5 rounded uppercase">SOCKET.IO LIVE</span>
                                        </div>
                                        <div className="max-h-72 overflow-y-auto divide-y divide-slate-850/60">
                                            {notifications.length === 0 ? (
                                                <div className="p-6 text-center text-[10px] text-slate-500 uppercase">
                                                    NO ACTIVE THREATS BROADCASTING
                                                </div>
                                            ) : (
                                                notifications.map((notif, i) => (
                                                    <div key={i} className="p-3.5 hover:bg-slate-850/40 transition-colors text-left space-y-1">
                                                        <div className="flex items-center justify-between">
                                                            <span className="text-[10px] font-bold text-rose-400 uppercase">{notif.type || 'Slippage Danger'}</span>
                                                            <span className="text-[8px] text-slate-500">{notif.time || 'Just now'}</span>
                                                        </div>
                                                        <p className="text-[10px] leading-relaxed text-slate-300">{notif.message}</p>
                                                    </div>
                                                ))
                                            )}
                                        </div>
                                    </motion.div>
                                )}
                            </AnimatePresence>
                        </div>

                        {/* Profile Avatar Dropdown */}
                        <div className="relative" ref={profileRef}>
                            <button
                                onClick={() => setShowProfileMenu(!showProfileMenu)}
                                className="flex items-center gap-2 p-1 bg-slate-900/60 hover:bg-slate-850/80 border border-slate-800 rounded-xl transition-all"
                            >
                                <img
                                    src={user.avatar}
                                    alt="User Profile"
                                    className="w-8 h-8 rounded-lg bg-slate-950 p-0.5"
                                />
                                <span className="hidden md:inline text-xs font-mono font-bold px-1 text-slate-300">▼</span>
                            </button>

                            <AnimatePresence>
                                {showProfileMenu && (
                                    <motion.div
                                        initial={{ opacity: 0, y: 10 }}
                                        animate={{ opacity: 1, y: 0 }}
                                        exit={{ opacity: 0, y: 10 }}
                                        className="absolute right-0 mt-3 w-56 bg-slate-900 border border-slate-800 rounded-2xl shadow-[0_10px_30px_rgba(0,0,0,0.5)] overflow-hidden z-50 font-mono"
                                    >
                                        <div className="p-4 bg-slate-950/40 border-b border-slate-800/80">
                                            <p className="text-xs font-bold text-slate-200 truncate">{user.name}</p>
                                            <p className="text-[9px] text-slate-500 truncate">{user.email}</p>
                                            <span className="inline-block mt-2 px-2 py-0.5 bg-cyan-950/50 border border-cyan-500/20 text-[8px] font-bold text-cyan-400 rounded-md uppercase tracking-wider">
                                                {user.plan}
                                            </span>
                                        </div>
                                        <div className="p-2 space-y-1">
                                            <div className="px-3 py-2 text-[9px] text-slate-500 uppercase tracking-widest">
                                                Joined: {user.joinedAt}
                                            </div>
                                            <button
                                                onClick={onLogout}
                                                className="w-full flex items-center gap-2 px-3 py-2 text-xs font-bold text-rose-400 hover:text-rose-300 hover:bg-rose-950/20 rounded-xl text-left transition-colors uppercase"
                                            >
                                                ✕ Terminate Session
                                            </button>
                                        </div>
                                    </motion.div>
                                )}
                            </AnimatePresence>
                        </div>
                    </div>
                </header>

                {/* Content Shell Frame */}
                <main className="flex-grow overflow-y-auto p-6 md:p-8 bg-slate-950">
                    <motion.div
                        key={activeTab}
                        initial={{ opacity: 0, scale: 0.98 }}
                        animate={{ opacity: 1, scale: 1 }}
                        transition={{ duration: 0.2 }}
                        className="h-full w-full max-w-7xl mx-auto"
                    >
                        {children}
                    </motion.div>
                </main>
            </div>
        </div>
    );
};

export default AppLayout;

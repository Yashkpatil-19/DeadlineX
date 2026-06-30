import React from 'react';
import { motion } from 'framer-motion';

const LandingPage = ({ onJoin }) => {
    const features = [
        {
            title: "Predictive Risk Analytics",
            description: "Advanced math engines continually evaluate task progress, estimating potential bottlenecks and calculating real-time failure probabilities before they occur.",
            icon: "📊",
            metric: "98% Accuracy",
            color: "text-cyan-400 border-cyan-500/20 shadow-[0_0_15px_rgba(34,211,238,0.1)]"
        },
        {
            title: "Progressive Skill Roadmaps",
            description: "Breaks complex, multi-day goals into daily progressive milestones, scaling automatically from novice-level instructions to master execution levels.",
            icon: "📈",
            metric: "Adaptive Scaling",
            color: "text-emerald-400 border-emerald-500/20 shadow-[0_0_15px_rgba(52,211,153,0.1)]"
        },
        {
            title: "AI Overdrive Pomodoro Engine",
            description: "Accelerate productivity with focus-sprint mechanisms that dynamically adapt to your pacing, keeping you aligned with aggressive target schedules.",
            icon: "⚡",
            metric: "3.5x Output Boost",
            color: "text-amber-400 border-amber-500/20 shadow-[0_0_15px_rgba(251,191,36,0.1)]"
        }
    ];

    const stats = [
        { label: "Deadlines Secured", value: "142,850+" },
        { label: "AI Roadmaps Generated", value: "389,400+" },
        { label: "Risk Mitigation Actions", value: "95.4%" },
        { label: "Emergency Interventions", value: "0% Slippage" }
    ];

    return (
        <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-cyan-500 selection:text-slate-900 overflow-x-hidden">
            {/* Header */}
            <header className="sticky top-0 z-50 bg-slate-950/80 backdrop-blur-md border-b border-slate-900 px-6 py-4 flex items-center justify-between">
                <div className="flex items-center gap-3">
                    <span className="text-xl">🛡️</span>
                    <div>
                        <h1 className="text-sm font-bold font-mono tracking-widest text-cyan-400">DEADLINEX</h1>
                        <p className="text-[9px] text-slate-500 font-mono tracking-wider uppercase">Autonomous AI Productivity Agent</p>
                    </div>
                </div>
                <button
                    onClick={onJoin}
                    className="px-4 py-2 bg-slate-900/60 hover:bg-slate-850 border border-slate-800 hover:border-cyan-500/30 text-xs font-bold font-mono text-cyan-400 tracking-wider uppercase rounded-xl transition-all shadow-md"
                >
                    LAUNCH PORTAL
                </button>
            </header>

            {/* Hero Section */}
            <main className="flex-grow flex flex-col items-center px-6 py-12 md:py-24 max-w-7xl mx-auto w-full relative">
                {/* Background ambient glows */}
                <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-cyan-500/5 rounded-full blur-[120px] pointer-events-none"></div>
                <div className="absolute top-1/3 left-1/4 w-[300px] h-[300px] bg-purple-500/5 rounded-full blur-[100px] pointer-events-none"></div>

                <div className="text-center space-y-6 max-w-3xl relative z-10">
                    <motion.div
                        initial={{ opacity: 0, y: -20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.6 }}
                        className="inline-flex items-center gap-2 px-3 py-1.5 bg-cyan-950/40 border border-cyan-500/20 rounded-full text-[10px] font-bold font-mono tracking-wider text-cyan-400 uppercase"
                    >
                        <span>🤖</span> COGNITIVE TIME PROTECTION PROTOCOL
                    </motion.div>

                    <motion.h2
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.7, delay: 0.1 }}
                        className="text-4xl md:text-6xl font-black tracking-tight leading-tight uppercase font-mono"
                    >
                        Deadline<span className="text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 via-teal-400 to-indigo-400 drop-shadow-[0_0_15px_rgba(34,211,238,0.2)]">X</span>
                    </motion.h2>

                    <motion.h3
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.7, delay: 0.2 }}
                        className="text-lg md:text-xl text-slate-400 font-mono font-medium max-w-2xl mx-auto"
                    >
                        The Autonomous AI Productivity Agent designed to calculate, adapt, and secure your schedules.
                    </motion.h3>

                    <motion.p
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.7, delay: 0.3 }}
                        className="text-sm text-slate-500 max-w-xl mx-auto"
                    >
                        Leverage the ultimate cybernetic scheduling interface. Auto-generate day-by-day subtask roadmaps, analyze delivery risk coefficients with real-time Socket.io triggers, and synch with Google Calendar.
                    </motion.p>

                    <motion.div
                        initial={{ opacity: 0, scale: 0.95 }}
                        animate={{ opacity: 1, scale: 1 }}
                        transition={{ duration: 0.5, delay: 0.4 }}
                        className="pt-6"
                    >
                        <button
                            onClick={onJoin}
                            className="group relative px-8 py-4 bg-gradient-to-r from-cyan-500 to-teal-500 hover:from-cyan-400 hover:to-teal-400 text-slate-950 font-bold font-mono tracking-widest text-sm rounded-2xl transition-all shadow-[0_0_30px_rgba(34,211,238,0.3)] hover:shadow-[0_0_40px_rgba(34,211,238,0.5)] active:scale-95 uppercase"
                        >
                            <span className="relative z-10 flex items-center justify-center gap-2">
                                Guard Your Deadlines Now <span>🛡️</span>
                            </span>
                            <span className="absolute inset-0 bg-white/20 rounded-2xl scale-0 group-hover:scale-100 transition-transform duration-300"></span>
                        </button>
                    </motion.div>
                </div>

                {/* Stats Row */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 w-full max-w-5xl mt-16 md:mt-24 border border-slate-900 bg-slate-950/50 backdrop-blur-sm p-6 rounded-2xl relative z-10">
                    {stats.map((stat, idx) => (
                        <div key={idx} className="text-center space-y-1">
                            <p className="text-2xl md:text-3xl font-black font-mono text-cyan-400">{stat.value}</p>
                            <p className="text-[10px] uppercase font-bold tracking-wider text-slate-500 font-mono">{stat.label}</p>
                        </div>
                    ))}
                </div>

                {/* Feature Showcase Grid */}
                <div className="w-full mt-24 md:mt-32 space-y-8 relative z-10">
                    <div className="text-center space-y-2">
                        <h3 className="text-xs font-bold font-mono tracking-widest text-cyan-400 uppercase">COGNITIVE ENGINE CAPABILITIES</h3>
                        <h2 className="text-2xl md:text-3xl font-black font-mono tracking-wide uppercase">AUTONOMOUS SCHEDULING MATRIX</h2>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                        {features.map((feature, idx) => (
                            <motion.div
                                key={idx}
                                whileHover={{ y: -8, transition: { duration: 0.2 } }}
                                className={`flex flex-col bg-slate-900/30 backdrop-blur-sm border p-6 rounded-2xl relative overflow-hidden transition-all duration-300 group ${feature.color}`}
                            >
                                <div className="absolute top-0 right-0 w-24 h-24 bg-gradient-to-br from-cyan-500/10 to-transparent blur-xl pointer-events-none transition-opacity duration-300 group-hover:opacity-100 opacity-50"></div>
                                
                                <div className="text-3xl mb-4">{feature.icon}</div>
                                <h4 className="text-lg font-black font-mono text-slate-100 mb-2 uppercase">{feature.title}</h4>
                                <p className="text-xs text-slate-400 leading-relaxed mb-6 flex-grow">{feature.description}</p>
                                
                                <div className="flex items-center justify-between border-t border-slate-800/60 pt-4 mt-auto">
                                    <span className="text-[9px] font-mono tracking-widest uppercase text-slate-500">Metric Indicator</span>
                                    <span className="text-xs font-bold font-mono tracking-wider">{feature.metric}</span>
                                </div>
                            </motion.div>
                        ))}
                    </div>
                </div>

                {/* Tactical Emergency Banner & Hackathon Card */}
                <div className="w-full max-w-5xl mt-24 md:mt-32 border border-rose-500/10 bg-gradient-to-b from-rose-950/10 to-transparent p-8 rounded-3xl relative overflow-hidden z-10 flex flex-col md:flex-row items-center justify-between gap-8">
                    <div className="absolute top-0 right-0 w-48 h-48 bg-rose-500/5 rounded-full blur-3xl pointer-events-none"></div>
                    <div className="space-y-3 max-w-xl">
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 bg-rose-950/50 border border-rose-500/20 text-[9px] font-bold font-mono text-rose-400 rounded-lg uppercase tracking-wider">
                            🚨 DEFCON RISK LEVEL 1
                        </span>
                        <h3 className="text-xl md:text-2xl font-black font-mono text-slate-100 uppercase">AI EMERGENCY RESPONSE PROTOCOLS</h3>
                        <p className="text-xs text-slate-400 leading-relaxed">
                            When target deadlines cross the high-risk coefficient threshold, DeadlineX overrides standard flows. It activates a strict, compromise-free Pomodoro rescue roadmap that structures every remaining hour for zero-slippage delivery.
                        </p>
                    </div>
                    <div className="text-center md:text-right flex flex-col gap-3 min-w-[200px]">
                        <div className="bg-slate-900/80 border border-slate-800 px-4 py-3.5 rounded-xl font-mono">
                            <span className="text-[9px] text-slate-500 uppercase font-bold block tracking-wider">LIFETIME COMPROMISE</span>
                            <span className="text-xl font-black text-rose-400 uppercase">FREE / INVENTIVE</span>
                            <span className="text-[10px] text-slate-400 block mt-1">Full Developer & API Access</span>
                        </div>
                        <button
                            onClick={onJoin}
                            className="w-full px-5 py-3 bg-slate-100 hover:bg-slate-200 text-slate-950 text-xs font-bold font-mono tracking-wider uppercase rounded-xl transition-all shadow-md active:scale-95"
                        >
                            CLAIM LICENSE ➔
                        </button>
                    </div>
                </div>
            </main>

            {/* Footer */}
            <footer className="border-t border-slate-900 bg-slate-950 px-6 py-8 mt-24 text-center space-y-3">
                <p className="text-xs text-slate-500 font-mono">
                    &copy; 2026 DeadlineX Co. Secure autonomous delivery engines.
                </p>
                <div className="flex justify-center gap-6 text-[10px] font-mono tracking-wider text-slate-500 uppercase">
                    <a href="#features" className="hover:text-cyan-400 transition-colors">API System</a>
                    <span>&bull;</span>
                    <a href="#security" className="hover:text-cyan-400 transition-colors">Crypto Guards</a>
                    <span>&bull;</span>
                    <a href="#terms" className="hover:text-cyan-400 transition-colors">Emergency Overrides</a>
                </div>
            </footer>
        </div>
    );
};

export default LandingPage;

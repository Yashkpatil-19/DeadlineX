import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

const UserGuideOverlay = ({ isOpen, onClose }) => {
    const [currentStep, setCurrentStep] = useState(0);

    if (!isOpen) return null;

    const handleNext = () => {
        if (currentStep < steps.length - 1) {
            setCurrentStep(currentStep + 1);
        } else {
            handleComplete();
        }
    };

    const handleBack = () => {
        if (currentStep > 0) {
            setCurrentStep(currentStep - 1);
        }
    };

    const handleComplete = () => {
        localStorage.setItem('hasSeenOnboarding', 'true');
        onClose();
    };

    const handleSkip = () => {
        handleComplete();
    };

    const steps = [
        {
            title: "Welcome to the Guardian Dashboard",
            description: "This is your proactive command center. Unlike static calendars, the AI analyzes your workload, difficulty, and upcoming dates to sort your tasks into high, medium, and low priority lists automatically.",
            color: "from-cyan-500 to-blue-600",
            accentColor: "text-cyan-400",
            glowColor: "rgba(34, 211, 238, 0.15)",
            illustration: (
                <svg className="w-full h-full" viewBox="0 0 200 120" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <defs>
                        <linearGradient id="dbGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stopColor="#06b6d4" />
                            <stop offset="100%" stopColor="#3b82f6" />
                        </linearGradient>
                        <filter id="glow">
                            <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
                            <feMerge>
                                <feMergeNode in="coloredBlur"/>
                                <feMergeNode in="SourceGraphic"/>
                            </feMerge>
                        </filter>
                    </defs>
                    {/* Background Grid */}
                    <rect width="200" height="120" rx="16" fill="#020617" stroke="#1e293b" strokeWidth="2"/>
                    <line x1="10" y1="30" x2="190" y2="30" stroke="#1e293b" strokeWidth="1"/>
                    <line x1="50" y1="30" x2="50" y2="110" stroke="#1e293b" strokeWidth="1"/>
                    
                    {/* Title block */}
                    <rect x="15" y="10" width="40" height="8" rx="4" fill="#1e293b" />
                    <circle cx="185" cy="14" r="3" fill="#10b981" filter="url(#glow)"/>
                    
                    {/* Columns representing Priorities */}
                    {/* High Priority column */}
                    <rect x="60" y="40" width="38" height="65" rx="8" fill="#111827" stroke="#334155" strokeWidth="1" />
                    <rect x="64" y="44" width="30" height="5" rx="2.5" fill="#f43f5e" fillOpacity="0.2" />
                    <rect x="68" y="55" width="22" height="18" rx="4" fill="#f43f5e" fillOpacity="0.8" filter="url(#glow)" />
                    <rect x="68" y="78" width="22" height="10" rx="4" fill="#334155" />
                    
                    {/* Medium Priority column */}
                    <rect x="105" y="40" width="38" height="65" rx="8" fill="#111827" stroke="#334155" strokeWidth="1" />
                    <rect x="109" y="44" width="30" height="5" rx="2.5" fill="#f59e0b" fillOpacity="0.2" />
                    <rect x="113" y="55" width="22" height="12" rx="4" fill="#f59e0b" fillOpacity="0.8" filter="url(#glow)" />
                    <rect x="113" y="72" width="22" height="14" rx="4" fill="#334155" />
                    <rect x="113" y="90" width="22" height="8" rx="4" fill="#334155" />

                    {/* Low Priority column */}
                    <rect x="150" y="40" width="38" height="65" rx="8" fill="#111827" stroke="#334155" strokeWidth="1" />
                    <rect x="154" y="44" width="30" height="5" rx="2.5" fill="#10b981" fillOpacity="0.2" />
                    <rect x="158" y="55" width="22" height="10" rx="4" fill="#10b981" fillOpacity="0.8" filter="url(#glow)" />
                    <rect x="158" y="70" width="22" height="8" rx="4" fill="#334155" />

                    {/* Dashboard sidebar decoration */}
                    <rect x="15" y="40" width="25" height="6" rx="3" fill="#334155" />
                    <rect x="15" y="52" width="20" height="6" rx="3" fill="#1e293b" />
                    <rect x="15" y="64" width="22" height="6" rx="3" fill="#1e293b" />
                </svg>
            )
        },
        {
            title: "AI Task Breakdown Engine",
            description: "When you add a large goal (like 'Build Hackathon App' in 5 days), our AI automatically maps out an achievable, day-by-day subtask checklist. No more feeling overwhelmed—just follow the daily steps.",
            color: "from-purple-500 to-indigo-600",
            accentColor: "text-purple-400",
            glowColor: "rgba(168, 85, 247, 0.15)",
            illustration: (
                <svg className="w-full h-full" viewBox="0 0 200 120" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <defs>
                        <filter id="purpleGlow">
                            <feGaussianBlur stdDeviation="3" result="blur"/>
                            <feMerge>
                                <feMergeNode in="blur"/>
                                <feMergeNode in="SourceGraphic"/>
                            </feMerge>
                        </filter>
                    </defs>
                    {/* Screen */}
                    <rect width="200" height="120" rx="16" fill="#020617" stroke="#1e293b" strokeWidth="2"/>
                    
                    {/* Goal Card Header */}
                    <rect x="20" y="15" width="160" height="24" rx="8" fill="#1e1b4b" stroke="#4338ca" strokeWidth="1" />
                    <text x="32" y="31" fill="#c084fc" fontSize="9" fontWeight="bold" fontFamily="monospace">⚡ GOAL: BUILD HACKATHON APP</text>
                    <rect x="150" y="21" width="22" height="12" rx="6" fill="#312e81" />
                    <circle cx="161" cy="27" r="2.5" fill="#a855f7" filter="url(#purpleGlow)"/>

                    {/* Connecting Nodes representing breakdown */}
                    <path d="M40 70 L95 70" stroke="#4338ca" strokeWidth="2" strokeDasharray="3 3"/>
                    <path d="M100 70 L155 70" stroke="#4338ca" strokeWidth="2" strokeDasharray="3 3"/>

                    {/* Day 1 checklist */}
                    <rect x="20" y="52" width="45" height="52" rx="8" fill="#111827" stroke="#1e293b" strokeWidth="1" />
                    <rect x="25" y="58" width="35" height="5" rx="2.5" fill="#a855f7" fillOpacity="0.3" />
                    <circle cx="30" cy="74" r="4" fill="#10b981" />
                    <path d="M28 74 L30 76 L33 72" stroke="#ffffff" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round"/>
                    <rect x="38" y="72" width="22" height="4" rx="2" fill="#475569" />
                    <circle cx="30" cy="88" r="4" fill="#10b981" />
                    <path d="M28 88 L30 90 L33 86" stroke="#ffffff" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round"/>
                    <rect x="38" y="86" width="22" height="4" rx="2" fill="#475569" />

                    {/* Day 2 checklist */}
                    <rect x="77" y="52" width="45" height="52" rx="8" fill="#111827" stroke="#312e81" strokeWidth="1" filter="url(#purpleGlow)" />
                    <rect x="82" y="58" width="35" height="5" rx="2.5" fill="#a855f7" />
                    <circle cx="87" cy="74" r="4" fill="#a855f7" fillOpacity="0.4" stroke="#a855f7" strokeWidth="1"/>
                    <rect x="95" y="72" width="22" height="4" rx="2" fill="#94a3b8" />
                    <circle cx="87" cy="88" r="4" fill="#a855f7" fillOpacity="0.4" stroke="#a855f7" strokeWidth="1"/>
                    <rect x="95" y="88" width="22" height="4" rx="2" fill="#475569" />

                    {/* Day 3 checklist */}
                    <rect x="135" y="52" width="45" height="52" rx="8" fill="#111827" stroke="#1e293b" strokeWidth="1" />
                    <rect x="140" y="58" width="35" height="5" rx="2.5" fill="#475569" fillOpacity="0.4" />
                    <circle cx="145" cy="74" r="4" fill="#334155" />
                    <rect x="153" y="72" width="22" height="4" rx="2" fill="#334155" />
                    <circle cx="145" cy="88" r="4" fill="#334155" />
                    <rect x="153" y="88" width="22" height="4" rx="2" fill="#334155" />
                </svg>
            )
        },
        {
            title: "Predictive Risk Analyzer",
            description: "The AI looks at your live progress versus the clock. It calculates your statistical probability of missing a deadline. If your probability drops too low, it shifts the system risk level to HIGH and alerts you.",
            color: "from-amber-500 to-orange-600",
            accentColor: "text-amber-400",
            glowColor: "rgba(245, 158, 11, 0.15)",
            illustration: (
                <svg className="w-full h-full" viewBox="0 0 200 120" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <defs>
                        <filter id="amberGlow">
                            <feGaussianBlur stdDeviation="4" result="blur"/>
                            <feMerge>
                                <feMergeNode in="blur"/>
                                <feMergeNode in="SourceGraphic"/>
                            </feMerge>
                        </filter>
                    </defs>
                    {/* Screen */}
                    <rect width="200" height="120" rx="16" fill="#020617" stroke="#1e293b" strokeWidth="2"/>
                    
                    {/* Radial Dial representing Risk/Confidence */}
                    <circle cx="100" cy="65" r="38" stroke="#1e293b" strokeWidth="6" fill="none"/>
                    {/* Success arc (Green) */}
                    <path d="M72 90 A38 38 0 0 1 100 27" stroke="#10b981" strokeWidth="6" strokeLinecap="round" fill="none"/>
                    {/* Warning arc (Yellow/Orange) */}
                    <path d="M100 27 A38 38 0 0 1 128 90" stroke="#f59e0b" strokeWidth="6" strokeLinecap="round" fill="none" filter="url(#amberGlow)"/>
                    
                    {/* Indicator Needle */}
                    <path d="M100 65 L118 40" stroke="#f43f5e" strokeWidth="3" strokeLinecap="round" filter="url(#amberGlow)"/>
                    <circle cx="100" cy="65" r="5" fill="#f43f5e" />

                    {/* Digital display */}
                    <rect x="75" y="88" width="50" height="18" rx="6" fill="#111827" stroke="#f59e0b" strokeWidth="1" />
                    <text x="100" y="100" fill="#f59e0b" fontSize="8" fontWeight="bold" fontFamily="monospace" textAnchor="middle">RISK: 74%</text>

                    {/* Stats details left */}
                    <rect x="15" y="30" width="40" height="12" rx="4" fill="#1e293b"/>
                    <rect x="20" y="34" width="20" height="4" rx="2" fill="#10b981"/>
                    <rect x="15" y="47" width="40" height="12" rx="4" fill="#1e293b"/>
                    <rect x="20" y="51" width="30" height="4" rx="2" fill="#f59e0b"/>

                    {/* Alerts right */}
                    <rect x="145" y="30" width="40" height="28" rx="6" fill="#7f1d1d" fillOpacity="0.4" stroke="#f43f5e" strokeWidth="1" />
                    <circle cx="165" cy="40" r="3" fill="#f43f5e" />
                    <rect x="153" y="48" width="24" height="4" rx="2" fill="#ffffff" />
                </svg>
            )
        },
        {
            title: "AI Emergency Mode 🚨",
            description: "If a crucial deadline is less than 6 hours away and your progress is low, Emergency Mode kicks in. The app shifts to a high-visibility rescue UI, re-routes all your plans, tells you exactly what daily habits to cancel, and maps out a minute-by-minute sprint.",
            color: "from-rose-600 to-red-700",
            accentColor: "text-rose-400",
            glowColor: "rgba(244, 63, 94, 0.2)",
            illustration: (
                <svg className="w-full h-full" viewBox="0 0 200 120" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <defs>
                        <filter id="roseGlow">
                            <feGaussianBlur stdDeviation="4" result="blur"/>
                            <feMerge>
                                <feMergeNode in="blur"/>
                                <feMergeNode in="SourceGraphic"/>
                            </feMerge>
                        </filter>
                    </defs>
                    {/* Emergency Mode Red Grid */}
                    <rect width="200" height="120" rx="16" fill="#090506" stroke="#991b1b" strokeWidth="2.5"/>
                    <rect x="4" y="4" width="192" height="112" rx="12" fill="none" stroke="#f43f5e" strokeWidth="0.5" strokeDasharray="4 4" opacity="0.6"/>
                    
                    {/* Big Siren Icon */}
                    <circle cx="100" cy="40" r="16" fill="#7f1d1d" stroke="#f43f5e" strokeWidth="1.5" />
                    <path d="M100 24 L100 32 M100 48 L100 56 M84 40 L92 40 M108 40 L116 40" stroke="#f43f5e" strokeWidth="2" strokeLinecap="round" filter="url(#roseGlow)"/>
                    <path d="M88 28 L94 34 M106 46 L112 52 M88 52 L94 46 M106 34 L112 28" stroke="#f43f5e" strokeWidth="2" strokeLinecap="round" opacity="0.5"/>
                    <circle cx="100" cy="40" r="8" fill="#f43f5e" filter="url(#roseGlow)"/>

                    {/* Canceled Habits Panel */}
                    <rect x="15" y="70" width="80" height="40" rx="8" fill="#1c0a0c" stroke="#991b1b" strokeWidth="1" />
                    <text x="23" y="82" fill="#f43f5e" fontSize="7" fontWeight="bold" fontFamily="monospace">🚫 BLOCK LIST</text>
                    
                    {/* Blocked item 1 */}
                    <rect x="23" y="88" width="40" height="4" rx="2" fill="#ef4444" opacity="0.4"/>
                    <circle cx="78" cy="90" r="3" fill="#ef4444" />
                    <line x1="76" y1="88" x2="80" y2="92" stroke="#ffffff" strokeWidth="1"/>

                    {/* Blocked item 2 */}
                    <rect x="23" y="98" width="48" height="4" rx="2" fill="#ef4444" opacity="0.4"/>
                    <circle cx="78" cy="100" r="3" fill="#ef4444" />
                    <line x1="76" y1="98" x2="80" y2="102" stroke="#ffffff" strokeWidth="1"/>

                    {/* Quick Sprint Timeline */}
                    <rect x="105" y="70" width="80" height="40" rx="8" fill="#111827" stroke="#334155" strokeWidth="1" />
                    <text x="113" y="82" fill="#38bdf8" fontSize="7" fontWeight="bold" fontFamily="monospace">⏱️ RESCUE SPRINT</text>
                    <path d="M115 95 L175 95" stroke="#475569" strokeWidth="1.5" />
                    <circle cx="125" cy="95" r="4" fill="#f43f5e" filter="url(#roseGlow)" />
                    <circle cx="150" cy="95" r="3" fill="#38bdf8" />
                    <circle cx="170" cy="95" r="3" fill="#10b981" />
                </svg>
            )
        },
        {
            title: "Voice Commands & Ambient Assistance",
            description: "Tap the microphone icon anywhere to chat with your agent natively. Tell it things like: 'I have an exam in 10 days' or 'Mark the landing page design as complete' to manage your schedule completely hands-free.",
            color: "from-cyan-500 to-teal-500",
            accentColor: "text-emerald-400",
            glowColor: "rgba(16, 185, 129, 0.15)",
            illustration: (
                <svg className="w-full h-full" viewBox="0 0 200 120" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <defs>
                        <filter id="emeraldGlow">
                            <feGaussianBlur stdDeviation="3" result="blur"/>
                            <feMerge>
                                <feMergeNode in="blur"/>
                                <feMergeNode in="SourceGraphic"/>
                            </feMerge>
                        </filter>
                    </defs>
                    {/* Screen */}
                    <rect width="200" height="120" rx="16" fill="#020617" stroke="#1e293b" strokeWidth="2"/>
                    
                    {/* Voice wave in the center */}
                    <path d="M50 60 Q62.5 40 75 60 T100 60 T125 60 T150 60" stroke="#10b981" strokeWidth="2" strokeLinecap="round" filter="url(#emeraldGlow)"/>
                    <path d="M50 60 Q62.5 25 75 60 T100 60 T125 60 T150 60" stroke="#06b6d4" strokeWidth="1.5" strokeLinecap="round" opacity="0.7"/>
                    <path d="M50 60 Q62.5 55 75 60 T100 60 T125 60 T150 60" stroke="#10b981" strokeWidth="1" strokeLinecap="round" opacity="0.4"/>

                    {/* Speech bubbles */}
                    {/* User bubble left-top */}
                    <rect x="15" y="15" width="70" height="18" rx="6" fill="#1e293b" />
                    <text x="21" y="26" fill="#94a3b8" fontSize="6.5">{"\"I have an exam in 10 days\""}</text>
                    
                    {/* Assistant response right-bottom */}
                    <rect x="105" y="85" width="80" height="18" rx="6" fill="#064e3b" stroke="#10b981" strokeWidth="0.5" />
                    <text x="111" y="96" fill="#34d399" fontSize="6.5">{"\"Analyzing workload... Roadmap active.\""}</text>

                    {/* Mic button glow */}
                    <circle cx="100" cy="60" r="14" fill="#042f2e" stroke="#10b981" strokeWidth="1" filter="url(#emeraldGlow)" />
                    <path d="M100 52 C98 52 97 53.5 97 55 L97 61 C97 62.5 98 64 100 64 C102 64 103 62.5 103 61 L103 55 C103 53.5 102 52 100 52 Z" fill="#10b981"/>
                    <path d="M95 59 L95 61 C95 63.8 97.2 66 100 66 C102.8 66 105 63.8 105 61 L105 59" stroke="#10b981" strokeWidth="1.5" strokeLinecap="round"/>
                    <line x1="100" y1="66" x2="100" y2="70" stroke="#10b981" strokeWidth="1.5"/>
                </svg>
            )
        }
    ];

    const currentStepData = steps[currentStep];

    return (
        <AnimatePresence>
            <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/85 backdrop-blur-md">
                {/* Backdrop closer */}
                <div className="absolute inset-0 cursor-default" onClick={handleSkip}></div>

                {/* Main Modal Card */}
                <motion.div 
                    initial={{ opacity: 0, scale: 0.92, y: 15 }}
                    animate={{ opacity: 1, scale: 1, y: 0 }}
                    exit={{ opacity: 0, scale: 0.95, y: 10 }}
                    transition={{ type: "spring", duration: 0.5, bounce: 0.15 }}
                    className="relative bg-slate-900/95 border border-slate-800 rounded-[28px] max-w-lg w-full shadow-2xl overflow-hidden flex flex-col z-10"
                    style={{
                        boxShadow: `0 20px 50px -12px ${currentStepData.glowColor || 'rgba(0,0,0,0.5)'}, inset 0 1px 1px 0 rgba(255,255,255,0.05)`
                    }}
                >
                    {/* Color Glow Overlay */}
                    <div className="absolute top-0 right-0 w-64 h-64 bg-gradient-to-br from-cyan-500/10 to-purple-500/0 rounded-full filter blur-3xl pointer-events-none"></div>
                    <div className="absolute -bottom-10 -left-10 w-48 h-48 bg-emerald-500/5 rounded-full filter blur-3xl pointer-events-none"></div>

                    {/* Step indicator top */}
                    <div className="flex justify-between items-center px-8 pt-6 pb-2 border-b border-slate-800/50">
                        <div className="flex items-center gap-1.5">
                            <span className="w-2 h-2 rounded-full bg-cyan-400"></span>
                            <span className="text-[10px] font-extrabold font-mono tracking-widest text-slate-400 uppercase">Guardian Guide</span>
                        </div>
                        <button 
                            onClick={handleSkip}
                            className="text-slate-500 hover:text-slate-300 transition-colors text-xs font-bold font-mono uppercase tracking-wider bg-slate-950/40 px-2.5 py-1 rounded-lg border border-slate-800/80"
                        >
                            Skip Tour
                        </button>
                    </div>

                    {/* Step Illustration Content */}
                    <div className="px-8 pt-6 pb-4">
                        <div className="w-full aspect-[1.7/1] rounded-2xl bg-slate-950 border border-slate-850 overflow-hidden flex items-center justify-center shadow-inner relative group">
                            <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-transparent to-transparent opacity-80 z-1"></div>
                            <AnimatePresence mode="wait">
                                <motion.div
                                    key={currentStep}
                                    initial={{ opacity: 0, scale: 0.95, x: 20 }}
                                    animate={{ opacity: 1, scale: 1, x: 0 }}
                                    exit={{ opacity: 0, scale: 0.95, x: -20 }}
                                    transition={{ duration: 0.35, ease: "easeInOut" }}
                                    className="w-full h-full p-4 flex items-center justify-center"
                                >
                                    {currentStepData.illustration}
                                </motion.div>
                            </AnimatePresence>
                        </div>
                    </div>

                    {/* Text Details Area */}
                    <div className="px-8 pb-6 flex-grow flex flex-col justify-between min-h-[170px]">
                        <div className="space-y-2">
                            {/* Step Dots indicator */}
                            <div className="flex gap-1.5 pt-1">
                                {steps.map((_, idx) => (
                                    <div 
                                        key={idx}
                                        onClick={() => setCurrentStep(idx)}
                                        className={`h-1.5 rounded-full transition-all duration-300 cursor-pointer ${
                                            idx === currentStep ? 'w-6 bg-gradient-to-r ' + currentStepData.color : 'w-1.5 bg-slate-800'
                                        }`}
                                    ></div>
                                ))}
                            </div>

                            <AnimatePresence mode="wait">
                                <motion.div
                                    key={currentStep}
                                    initial={{ opacity: 0, y: 10 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    exit={{ opacity: 0, y: -10 }}
                                    transition={{ duration: 0.25 }}
                                    className="space-y-2"
                                >
                                    <h3 className="text-xl font-black text-slate-100 tracking-tight leading-tight flex items-center gap-2 mt-2">
                                        <span className={`text-base font-bold font-mono ${currentStepData.accentColor}`}>0{currentStep + 1}.</span>
                                        {currentStepData.title}
                                    </h3>
                                    <p className="text-slate-400 text-sm leading-relaxed font-normal">
                                        {currentStepData.description}
                                    </p>
                                </motion.div>
                            </AnimatePresence>
                        </div>

                        {/* Navigation Actions Footer */}
                        <div className="flex justify-between items-center mt-6 pt-4 border-t border-slate-850/60">
                            <button
                                onClick={handleBack}
                                disabled={currentStep === 0}
                                className={`text-xs font-bold font-mono tracking-wider uppercase py-2 px-4 rounded-xl transition-all border ${
                                    currentStep === 0 
                                    ? 'text-slate-700 border-transparent cursor-not-allowed opacity-0' 
                                    : 'text-slate-400 border-slate-800 hover:border-slate-700 hover:bg-slate-950/50 hover:text-slate-200'
                                }`}
                            >
                                &lt; Back
                            </button>

                            <button
                                onClick={handleNext}
                                className={`flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r ${currentStepData.color} text-xs font-bold font-mono tracking-wider uppercase rounded-xl transition-all hover:brightness-110 active:scale-97 shadow-lg text-white`}
                                style={{
                                    boxShadow: `0 4px 15px -4px ${currentStepData.glowColor || 'rgba(0,0,0,0)'}`
                                }}
                            >
                                <span>{currentStep === steps.length - 1 ? "Finish Tour" : "Next Step"}</span>
                                <span>&gt;</span>
                            </button>
                        </div>
                    </div>
                </motion.div>
            </div>
        </AnimatePresence>
    );
};

export default UserGuideOverlay;

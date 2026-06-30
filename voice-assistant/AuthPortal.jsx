import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

const AuthPortal = ({ onLogin, onBackToLanding }) => {
    const [isLogin, setIsLogin] = useState(true);
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [fullName, setFullName] = useState('');
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);

    const validateForm = () => {
        const newErrors = {};
        if (!email) {
            newErrors.email = 'Email address is required';
        } else if (!/\S+@\S+\.\S+/.test(email)) {
            newErrors.email = 'Please provide a valid email format';
        }

        if (!password) {
            newErrors.password = 'Password is required';
        } else if (password.length < 6) {
            newErrors.password = 'Password must be at least 6 characters';
        }

        if (!isLogin && !fullName) {
            newErrors.fullName = 'Full name is required';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!validateForm()) return;

        setIsLoading(true);
        // Simulate premium SaaS authentication delays
        setTimeout(() => {
            setIsLoading(false);
            localStorage.setItem('deadlinex_auth_user', JSON.stringify({
                email,
                name: isLogin ? (email.split('@')[0]) : fullName,
                avatar: `https://api.dicebear.com/7.x/bottts/svg?seed=${encodeURIComponent(email)}`,
                plan: 'Premium Developer Mode',
                joinedAt: new Date().toLocaleDateString()
            }));
            onLogin(email);
        }, 800);
    };

    const handleGoogleOAuth = () => {
        setIsLoading(true);
        setTimeout(() => {
            setIsLoading(false);
            const userEmail = "developer.premium@gmail.com";
            localStorage.setItem('deadlinex_auth_user', JSON.stringify({
                email: userEmail,
                name: "Premium Sync User",
                avatar: `https://api.dicebear.com/7.x/bottts/svg?seed=google_user`,
                plan: 'Enterprise Calendar Suite',
                joinedAt: new Date().toLocaleDateString()
            }));
            // Simulate calendar connection parameter redirect
            const newUrl = window.location.protocol + "//" + window.location.host + window.location.pathname + "?calendar_connected=true";
            window.history.pushState({ path: newUrl }, '', newUrl);
            onLogin(userEmail);
        }, 600);
    };

    return (
        <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-center items-center px-4 relative overflow-hidden font-sans">
            {/* Ambient background blur circles */}
            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[400px] h-[400px] bg-cyan-500/10 rounded-full blur-[100px] pointer-events-none"></div>
            <div className="absolute -top-10 -left-10 w-[200px] h-[200px] bg-purple-500/5 rounded-full blur-[80px] pointer-events-none"></div>

            <button
                onClick={onBackToLanding}
                className="absolute top-6 left-6 flex items-center gap-2 px-3 py-1.5 bg-slate-900/60 hover:bg-slate-850 border border-slate-800 hover:border-slate-700 text-[10px] font-bold font-mono text-slate-400 hover:text-slate-200 tracking-wider uppercase rounded-lg transition-all"
            >
                🪶 Back To Home
            </button>

            <div className="w-full max-w-md relative z-10 space-y-8">
                {/* Brand header */}
                <div className="text-center space-y-2">
                    <span className="text-3xl block">🛡️</span>
                    <h2 className="text-2xl font-black font-mono tracking-widest text-cyan-400 uppercase">DEADLINEX</h2>
                    <p className="text-[10px] text-slate-500 font-mono tracking-widest uppercase">Secure Identity Gateway</p>
                </div>

                <AnimatePresence mode="wait">
                    <motion.div
                        key={isLogin ? 'signin' : 'signup'}
                        initial={{ opacity: 0, y: 15 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -15 }}
                        transition={{ duration: 0.25 }}
                        className="bg-slate-900/50 backdrop-blur-md border border-slate-800/80 p-8 rounded-2xl shadow-[0_0_40px_rgba(34,211,238,0.05)]"
                    >
                        <h3 className="text-md font-bold font-mono tracking-wider uppercase text-slate-200 mb-6">
                            {isLogin ? 'SIGN IN PROTOCOL' : 'PROVISION NEW CREDENTIALS'}
                        </h3>

                        <form onSubmit={handleSubmit} className="space-y-4">
                            {!isLogin && (
                                <div className="space-y-1">
                                    <label className="text-[10px] font-mono font-bold tracking-wider uppercase text-slate-400 block">Full Name</label>
                                    <input
                                        type="text"
                                        value={fullName}
                                        onChange={(e) => setFullName(e.target.value)}
                                        placeholder="Enter full name"
                                        className={`w-full px-4 py-3 bg-slate-950 border rounded-xl text-xs font-mono text-slate-200 placeholder-slate-600 focus:outline-none focus:ring-1 transition-all ${
                                            errors.fullName ? 'border-rose-500/50 focus:ring-rose-500 focus:border-rose-500' : 'border-slate-800 focus:ring-cyan-500/40 focus:border-cyan-500'
                                        }`}
                                    />
                                    {errors.fullName && <p className="text-[10px] text-rose-500 font-mono mt-1">{errors.fullName}</p>}
                                </div>
                            )}

                            <div className="space-y-1">
                                <label className="text-[10px] font-mono font-bold tracking-wider uppercase text-slate-400 block">Email Address</label>
                                <input
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    placeholder="your.address@domain.com"
                                    className={`w-full px-4 py-3 bg-slate-950 border rounded-xl text-xs font-mono text-slate-200 placeholder-slate-600 focus:outline-none focus:ring-1 transition-all ${
                                        errors.email ? 'border-rose-500/50 focus:ring-rose-500 focus:border-rose-500' : 'border-slate-800 focus:ring-cyan-500/40 focus:border-cyan-500'
                                    }`}
                                />
                                {errors.email && <p className="text-[10px] text-rose-500 font-mono mt-1">{errors.email}</p>}
                            </div>

                            <div className="space-y-1">
                                <label className="text-[10px] font-mono font-bold tracking-wider uppercase text-slate-400 block">Passkey Credentials</label>
                                <input
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    placeholder="••••••••"
                                    className={`w-full px-4 py-3 bg-slate-950 border rounded-xl text-xs font-mono text-slate-200 placeholder-slate-600 focus:outline-none focus:ring-1 transition-all ${
                                        errors.password ? 'border-rose-500/50 focus:ring-rose-500 focus:border-rose-500' : 'border-slate-800 focus:ring-cyan-500/40 focus:border-cyan-500'
                                    }`}
                                />
                                {errors.password && <p className="text-[10px] text-rose-500 font-mono mt-1">{errors.password}</p>}
                            </div>

                            <div className="pt-2">
                                <button
                                    type="submit"
                                    disabled={isLoading}
                                    className="w-full relative px-6 py-3.5 bg-gradient-to-r from-cyan-500 to-teal-500 hover:from-cyan-400 hover:to-teal-400 text-slate-950 font-bold font-mono tracking-widest text-xs rounded-xl transition-all shadow-lg hover:shadow-[0_0_15px_rgba(34,211,238,0.2)] disabled:opacity-50 active:scale-[0.98] uppercase"
                                >
                                    {isLoading ? (
                                        <span className="flex items-center justify-center gap-2">
                                            <span className="animate-spin text-sm">⏳</span> INJECTING SECURITY TOKEN...
                                        </span>
                                    ) : (
                                        <span>{isLogin ? 'INITIALIZE ACTIVE SESSION' : 'ESTABLISH NEW ENDPOINT'}</span>
                                    )}
                                </button>
                            </div>
                        </form>

                        <div className="relative my-6 text-center">
                            <span className="absolute inset-x-0 top-1/2 -translate-y-1/2 border-t border-slate-800/80"></span>
                            <span className="relative bg-slate-900/90 px-3 text-[10px] font-mono text-slate-500 tracking-wider uppercase">OR PROTOCOL CONTROL</span>
                        </div>

                        {/* Google OAuth Option */}
                        <button
                            onClick={handleGoogleOAuth}
                            disabled={isLoading}
                            className="w-full flex items-center justify-center gap-2 px-5 py-3.5 bg-slate-950 hover:bg-slate-900 border border-slate-800 hover:border-slate-700 text-xs font-mono text-slate-300 rounded-xl transition-all active:scale-[0.98]"
                        >
                            <span>📅</span>
                            <span>CONTINUE WITH GOOGLE CALENDAR</span>
                        </button>

                        {/* Toggle Account mode */}
                        <div className="mt-6 text-center">
                            <button
                                onClick={() => {
                                    setIsLogin(!isLogin);
                                    setErrors({});
                                }}
                                className="text-[10px] font-mono text-cyan-400 hover:text-cyan-300 tracking-wider uppercase hover:underline"
                            >
                                {isLogin ? "DON'T HAVE AN ENDPOINT? CREATE PROFILE" : "ALREADY ENROLLED? SIGN IN HERE"}
                            </button>
                        </div>
                    </motion.div>
                </AnimatePresence>
            </div>
        </div>
    );
};

export default AuthPortal;

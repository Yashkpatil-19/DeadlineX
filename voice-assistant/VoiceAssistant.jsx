import React, { useState, useEffect, useRef } from 'react';

const VoiceAssistant = ({ userId = "default-user" }) => {
    const [isListening, setIsListening] = useState(false);
    const [transcript, setTranscript] = useState("");
    const [status, setStatus] = useState("Idle"); // Idle, Listening, Processing, Speaking, Error
    const [errorMessage, setErrorMessage] = useState("");
    const [aiResponse, setAiResponse] = useState("");
    
    const recognitionRef = useRef(null);
    const synthRef = useRef(null);

    useEffect(() => {
        // Initialize Speech Synthesis
        if (typeof window !== "undefined") {
            synthRef.current = window.speechSynthesis;
        }

        // Initialize Speech Recognition
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (!SpeechRecognition) {
            setStatus("Error");
            setErrorMessage("Speech recognition is not supported in this browser. Try Chrome or Safari.");
            return;
        }

        const rec = new SpeechRecognition();
        rec.continuous = false;
        rec.interimResults = true;
        rec.lang = 'en-US';

        rec.onstart = () => {
            setIsListening(true);
            setStatus("Listening...");
            setErrorMessage("");
            setTranscript("");
        };

        rec.onresult = (event) => {
            const currentTranscript = Array.from(event.results)
                .map(result => result[0].transcript)
                .join("");
            setTranscript(currentTranscript);
        };

        rec.onerror = (event) => {
            console.error("Speech recognition error", event.error);
            setIsListening(false);
            setStatus("Error");
            if (event.error === 'not-allowed') {
                setErrorMessage("Microphone permission denied. Please allow microphone access in your browser settings.");
            } else {
                setErrorMessage(`Error: ${event.error}`);
            }
        };

        rec.onend = () => {
            setIsListening(false);
            // Only trigger processing if we have actual text
            if (transcript.trim()) {
                sendTranscriptToBackend(transcript.trim());
            } else {
                setStatus("Idle");
            }
        };

        recognitionRef.current = rec;

        return () => {
            if (recognitionRef.current) {
                recognitionRef.current.abort();
            }
            if (synthRef.current) {
                synthRef.current.cancel();
            }
        };
    }, [transcript]);

    const startListening = () => {
        if (synthRef.current && synthRef.current.speaking) {
            synthRef.current.cancel();
        }
        if (recognitionRef.current) {
            try {
                recognitionRef.current.start();
            } catch (err) {
                console.error("Failed to start speech recognition", err);
            }
        }
    };

    const stopListening = () => {
        if (recognitionRef.current) {
            recognitionRef.current.stop();
        }
    };

    const toggleListening = () => {
        if (isListening) {
            stopListening();
        } else {
            startListening();
        }
    };

    const sendTranscriptToBackend = async (textToSend) => {
        setStatus("Processing...");
        try {
            const response = await fetch('/api/voice/command', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    transcript: textToSend,
                    userId: userId
                })
            });

            if (!response.ok) {
                throw new Error(`Server returned code ${response.status}`);
            }

            const data = await response.json();
            setAiResponse(data.speakableResponse);
            speakResponse(data.speakableResponse);
        } catch (err) {
            console.error("Backend error", err);
            setStatus("Error");
            setErrorMessage(`Failed to connect to assistant: ${err.message}`);
        }
    };

    const speakResponse = (text) => {
        if (!synthRef.current) return;

        // Cancel current speaking
        synthRef.current.cancel();

        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = 'en-US';

        utterance.onstart = () => {
            setStatus("Speaking...");
        };

        utterance.onend = () => {
            setStatus("Idle");
        };

        utterance.onerror = (e) => {
            console.error("Speech synthesis error", e);
            setStatus("Idle");
        };

        // Select a premium natural voice if available
        const voices = synthRef.current.getVoices();
        const googleVoice = voices.find(voice => voice.name.includes("Google US English") || voice.name.includes("Natural"));
        if (googleVoice) {
            utterance.voice = googleVoice;
        }

        synthRef.current.speak(utterance);
    };

    return (
        <div className="flex flex-col items-center justify-center p-6 bg-slate-950 border border-slate-800 rounded-3xl shadow-2xl max-w-md w-full mx-auto relative overflow-hidden">
            {/* Background Decorative Glow */}
            <div className="absolute top-0 left-1/2 -translate-x-1/2 w-48 h-48 bg-cyan-500/10 rounded-full filter blur-3xl -z-10 pointer-events-none"></div>

            {/* Title Block */}
            <div className="text-center mb-6">
                <h3 className="text-lg font-bold text-slate-100 tracking-wider uppercase font-mono">
                    Deadline Guardian
                </h3>
                <p className="text-xs text-cyan-400 font-semibold tracking-wider uppercase">
                    AI Voice Assistant
                </p>
            </div>

            {/* Glowing Interactive Microphone Button */}
            <div className="relative flex items-center justify-center w-36 h-36 mb-6">
                {isListening && (
                    <span className="absolute inset-0 rounded-full bg-cyan-500/20 animate-ping duration-1000"></span>
                )}
                {status === "Speaking..." && (
                    <span className="absolute inset-2 rounded-full bg-indigo-500/20 animate-pulse duration-700"></span>
                )}
                <button
                    onClick={toggleListening}
                    className={`relative z-10 flex items-center justify-center w-28 h-28 rounded-full shadow-lg transition-all duration-300 transform hover:scale-105 active:scale-95 ${
                        isListening
                            ? "bg-gradient-to-tr from-rose-500 to-pink-600 shadow-rose-500/30"
                            : status === "Speaking..."
                            ? "bg-gradient-to-tr from-indigo-500 to-cyan-500 shadow-indigo-500/30"
                            : "bg-gradient-to-tr from-slate-800 to-slate-900 border border-slate-700 hover:border-cyan-500/50 shadow-black/50"
                    }`}
                >
                    {isListening ? (
                        // Recording / Stop Icon
                        <svg className="w-10 h-10 text-white animate-pulse" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <rect x="6" y="6" width="12" height="12" rx="2" strokeWidth={2} />
                        </svg>
                    ) : (
                        // Mic Icon
                        <svg className="w-10 h-10 text-cyan-400 hover:text-cyan-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z" />
                        </svg>
                    )}
                </button>
            </div>

            {/* AI Status Indicator */}
            <div className="flex items-center gap-2 mb-4 px-3 py-1 bg-slate-900/60 rounded-full border border-slate-800">
                <span className={`w-2.5 h-2.5 rounded-full ${
                    isListening
                        ? "bg-rose-500 animate-pulse"
                        : status === "Processing..."
                        ? "bg-amber-500 animate-bounce"
                        : status === "Speaking..."
                        ? "bg-indigo-500 animate-pulse"
                        : status === "Error"
                        ? "bg-red-500"
                        : "bg-slate-600"
                }`}></span>
                <span className="text-xs font-semibold text-slate-300 font-mono">
                    {status}
                </span>
            </div>

            {/* Live Transcript Panel */}
            <div className="w-full bg-slate-900 border border-slate-800/80 rounded-2xl p-4 min-h-[80px] mb-4 flex flex-col justify-between">
                <p className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">
                    Your Command
                </p>
                <p className={`text-sm leading-relaxed ${transcript ? 'text-slate-200' : 'text-slate-500 italic'}`}>
                    {transcript || 'Click the microphone and start speaking...'}
                </p>
            </div>

            {/* AI Speakable Response Panel */}
            {aiResponse && (
                <div className="w-full bg-indigo-950/20 border border-indigo-500/20 rounded-2xl p-4 min-h-[80px] mb-2">
                    <p className="text-[10px] font-bold text-indigo-400 uppercase tracking-widest mb-1">
                        AI Assistant
                    </p>
                    <p className="text-sm text-slate-300 leading-relaxed">
                        {aiResponse}
                    </p>
                </div>
            )}

            {/* Error Message banner */}
            {errorMessage && (
                <div className="mt-2 w-full p-3 bg-red-950/30 border border-red-500/30 rounded-xl">
                    <p className="text-xs text-red-400 leading-snug">
                        {errorMessage}
                    </p>
                </div>
            )}

            {/* Usage Tip */}
            <p className="mt-4 text-[10px] text-slate-500 text-center leading-normal">
                {"Try: \"Add exam prep with 5 days remaining\" or \"Update task progress by 15 percent\"."}
            </p>
        </div>
    );
};

export default VoiceAssistant;

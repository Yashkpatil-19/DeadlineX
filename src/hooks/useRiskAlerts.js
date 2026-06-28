import { useEffect, useRef } from 'react';
import { io } from 'socket.io-client';

/**
 * Custom hook that connects to the Real-Time Socket.io Server,
 * listens for critical risk alert events, and triggers native push notifications,
 * aggressive hardware haptics, and a UI callback.
 * 
 * @param {string|number} userId - The authenticated user ID
 * @param {string} serverUrl - The Socket.io server connection URL
 * @param {function} onVisualAlertTriggered - Callback to update global UI state for alert banners
 */
export function useRiskAlerts(userId, serverUrl, onVisualAlertTriggered) {
    const socketRef = useRef(null);

    useEffect(() => {
        if (!userId) return;

        // 1. Automatically request system notification permissions
        if ('Notification' in window) {
            if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
                Notification.requestPermission().then((permission) => {
                    console.log(`Notification permission status registered: ${permission}`);
                });
            }
        }

        // 2. Establish connection to Socket.io server
        const socket = io(serverUrl || window.location.origin, {
            transports: ['websocket'],
            autoConnect: true,
        });
        socketRef.current = socket;

        socket.on('connect', () => {
            console.log('Real-Time alerts socket connected successfully');
            // Register session with current userId
            socket.emit('register_user', userId);
        });

        // 3. Listen for high-alert "risk_alert_critical" event
        socket.on('risk_alert_critical', (data) => {
            const { taskTitle, completionProbability } = data;
            
            // A. Visual System Notification
            if ('Notification' in window && Notification.permission === 'granted') {
                new Notification("🚨 CRITICAL DEADLINE RISK", {
                    body: `${taskTitle} has a ${completionProbability}% chance of completion. Emergency action required!`,
                    icon: '/favicon.ico',
                    tag: `risk-alert-${data.taskId}`,
                    requireInteraction: true
                });
            }

            // B. Aggressive Device Vibration (Web Vibration API pattern)
            if ('vibrate' in navigator) {
                navigator.vibrate([500, 200, 500, 200, 1000]);
            }

            // C. UI State Dispatch Call
            if (onVisualAlertTriggered) {
                onVisualAlertTriggered({
                    show: true,
                    title: "🚨 CRITICAL RISK DETECTED",
                    message: `Predictive Risk Analyzer flagged "${taskTitle}" (${completionProbability}% completion chance)`,
                    task: data
                });
            }
        });

        socket.on('disconnect', () => {
            console.log('Alerts socket disconnected');
        });

        // 4. Return cleanup function to close socket and prevent memory leaks
        return () => {
            if (socketRef.current) {
                socketRef.current.disconnect();
                socketRef.current = null;
            }
        };
    }, [userId, serverUrl, onVisualAlertTriggered]);

    return socketRef.current;
}

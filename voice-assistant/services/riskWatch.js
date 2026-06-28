/**
 * Real-time Task Risk Controller
 * Checks if task's calculated completion probability drops below critical thresholds
 * and emits a high-alert socket event to the user's specific socket connection.
 */
function checkTaskRiskStatus(io, task, userSocketId) {
    if (!task) return;

    const riskLevel = task.riskLevel || "LOW";
    const completionProbability = typeof task.completionProbability === 'number' 
        ? task.completionProbability 
        : 100;

    // If task is completed, we do not need to flag risk alerts
    if (task.status === "COMPLETED") {
        return;
    }

    // Trigger immediately if Predictive Risk Analyzer flags task as "HIGH" risk
    if (riskLevel === "HIGH" || completionProbability < 40) {
        console.log(`[RISK ALERT] Broadcaster: Emitting critical alert for task "${task.title}" to socket ${userSocketId}`);
        io.to(userSocketId).emit("risk_alert_critical", {
            taskId: task.id,
            taskTitle: task.title,
            completionProbability: completionProbability,
            riskLevel: "HIGH"
        });
    }
}

module.exports = { checkTaskRiskStatus };

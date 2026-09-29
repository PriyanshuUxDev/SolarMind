package com.solarmind.dto.response; import java.util.List; public record DashboardResponse(AssessmentResponse latest,List<AssessmentSummary> recentAssessments){}

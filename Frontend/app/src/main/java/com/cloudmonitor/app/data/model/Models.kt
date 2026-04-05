package com.cloudmonitor.app.data.model

import com.google.gson.annotations.SerializedName

// ── Auth ──────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val user: UserInfo?,
    val error: String?
)

data class UserInfo(
    val username: String,
    val role: String
)

// ── Health ────────────────────────────────────────────────────────────────────

data class HealthResponse(
    val status: String,
    val version: String?,
    val uptime: String?,
    val node: String?,
    val env: String?,
    val aws: AwsHealth?,
    val github: GithubHealth?
)

data class AwsHealth(
    val credentialsConfigured: Boolean,
    val region: String?
)

data class GithubHealth(
    val tokenConfigured: Boolean
)

// ── EC2 ───────────────────────────────────────────────────────────────────────

data class Ec2Response(
    val success: Boolean,
    val count: Int,
    val mock: Boolean?,
    val instances: List<Ec2Instance>
)

data class Ec2Instance(
    val instanceId: String,
    val instanceType: String,
    val state: String,
    val publicIp: String?,
    val privateIp: String?,
    val publicDns: String?,
    val launchTime: String?,
    val availabilityZone: String?,
    val vpcId: String?,
    val subnetId: String?,
    val imageId: String?,
    val keyName: String?,
    val name: String?,
    val platform: String?,
    val architecture: String?,
    val securityGroups: List<SecurityGroupRef>?
)

data class SecurityGroupRef(
    val id: String,
    val name: String?
)

// ── S3 ────────────────────────────────────────────────────────────────────────

data class S3Response(
    val success: Boolean,
    val count: Int,
    val mock: Boolean?,
    val owner: String?,
    val buckets: List<S3Bucket>
)

data class S3Bucket(
    val name: String,
    val creationDate: String?
)

// ── Lambda ────────────────────────────────────────────────────────────────────

data class LambdaResponse(
    val success: Boolean,
    val count: Int,
    val mock: Boolean?,
    val functions: List<LambdaFunction>
)

data class LambdaFunction(
    val functionName: String,
    val functionArn: String?,
    val runtime: String?,
    val handler: String?,
    val codeSize: Long?,
    val description: String?,
    val lastModified: String?,
    val memorySize: Int?,
    val timeout: Int?,
    val state: String?
)

// ── EBS ───────────────────────────────────────────────────────────────────────

data class EbsResponse(
    val success: Boolean,
    val count: Int,
    val mock: Boolean?,
    val volumes: List<EbsVolume>
)

data class EbsVolume(
    val volumeId: String,
    val state: String,
    val size: Int?,
    val volumeType: String?,
    val iops: Int?,
    val encrypted: Boolean?,
    val availabilityZone: String?,
    val createTime: String?,
    val attachments: List<VolumeAttachment>?,
    val tags: Map<String, String>?
)

data class VolumeAttachment(
    val instanceId: String?,
    val device: String?,
    val state: String?
)

// ── VPC ───────────────────────────────────────────────────────────────────────

data class VpcResponse(
    val success: Boolean,
    val count: Int,
    val mock: Boolean?,
    val vpcs: List<Vpc>
)

data class Vpc(
    val vpcId: String,
    val state: String?,
    val cidrBlock: String?,
    val isDefault: Boolean?,
    val name: String?,
    val instanceTenancy: String?
)

data class SubnetResponse(
    val success: Boolean,
    val count: Int,
    val subnets: List<Subnet>
)

data class Subnet(
    val subnetId: String,
    val vpcId: String?,
    val state: String?,
    val cidrBlock: String?,
    val availabilityZone: String?,
    val availableIpAddressCount: Int?,
    val mapPublicIpOnLaunch: Boolean?,
    val name: String?
)

data class SecurityGroupResponse(
    val success: Boolean,
    val count: Int,
    val securityGroups: List<SecurityGroup>
)

data class SecurityGroup(
    val groupId: String,
    val groupName: String?,
    val description: String?,
    val vpcId: String?,
    val inboundRules: List<SgRule>?,
    val outboundRules: List<SgRule>?
)

data class SgRule(
    val protocol: String?,
    val fromPort: Int?,
    val toPort: Int?,
    val ipRanges: List<String>?
)

// ── Monitoring ────────────────────────────────────────────────────────────────

data class MonitoringResponse(
    val success: Boolean,
    val mock: Boolean?,
    val instanceId: String,
    val periodSecs: Int?,
    val hoursBack: Int?,
    val metrics: MetricsData?
)

data class MetricsData(
    val cpuUtilization: MetricSeries?,
    val networkIn: MetricSeries?,
    val networkOut: MetricSeries?,
    val diskReadBytes: MetricSeries?,
    val diskWriteBytes: MetricSeries?
)

data class MetricSeries(
    val metricName: String?,
    val stat: String?,
    val unit: String?,
    val datapoints: List<Datapoint>?
)

data class Datapoint(
    val timestamp: String?,
    val value: Double?,
    val unit: String?
)

// ── Cost ──────────────────────────────────────────────────────────────────────

data class CostResponse(
    val success: Boolean,
    val mock: Boolean?,
    val granularity: String?,
    val period: CostPeriod?,
    val totalCost: String?,
    val currency: String?,
    val serviceCount: Int?,
    val services: List<ServiceCost>
)

data class CostPeriod(
    val start: String,
    val end: String
)

data class ServiceCost(
    val service: String,
    val amount: String,
    val unit: String
)

// ── CI/CD ─────────────────────────────────────────────────────────────────────

data class CicdRunsResponse(
    val success: Boolean,
    val owner: String?,
    val repo: String?,
    val totalCount: Int?,
    val runs: List<WorkflowRun>
)

data class WorkflowRun(
    val id: Long,
    val name: String?,
    val status: String?,
    val conclusion: String?,
    val branch: String?,
    val commitSha: String?,
    val commitMessage: String?,
    val actor: String?,
    val triggerEvent: String?,
    val runNumber: Int?,
    val runAttempt: Int?,
    val createdAt: String?,
    val updatedAt: String?,
    val url: String?,
    val durationMs: Long?
)

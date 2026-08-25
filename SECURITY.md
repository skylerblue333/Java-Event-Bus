# Security Policy

Sky Java Event Bus is an engineering-beta in-process library, not a network or authorization boundary.

Event identifiers, topic names, attributes, payloads, topic count, and subscribers per topic are bounded. The implementation loads no dynamic plugins and performs no reflection-based handler lookup, shell execution, network access, or deserialization of executable content. The container runs as a non-root user.

Subscriber functions execute with the same JVM permissions as the publisher. Do not register untrusted code. This library does not authenticate publishers/subscribers, authorize topics, encrypt events, isolate tenants, persist an audit log, sandbox handlers, enforce timeouts, or provide distributed security controls. Use a separately verified broker/gateway for cross-process or untrusted messaging.

Report vulnerabilities privately through GitHub security reporting when available. Do not publish credentials, private event data, or working exploit details in public issues.

# Computer Networks Projects

Three projects from the Computer Networks course (COMP416) at Koç University, Fall 2025. Each folder has its own report with full details.

## 1. `ocm-client-server` — custom application-layer protocol

A Java client–server application that finds the nearest electric-car charging stations using the [Open Charge Map](https://openchargemap.org) API. I designed a text protocol (OCM416) with status codes; the multithreaded server validates requests, queries the REST API, parses the JSON with Gson and returns formatted results to a command-line client.

```bash
export OCM_API_KEY=<your Open Charge Map API key>
# run OCMServer with a port, e.g. 8080, then OCMClient with the same port
```

## 2. `tcp-vs-ssl` — transport-layer analysis

A sensor-data streaming server and client over plain TCP (port 8000) and SSL/TLS (port 8001), with a benchmark comparing the two, plus a Wireshark analysis of TCP, UDP and QUIC. The SSL keystore and truststore are not included; generate your own with `keytool`.

## 3. `network-layer` — ICMP and network design

Traceroute/ICMP analysis and a network designed and simulated in Cisco Packet Tracer (`network_design.pkt`).

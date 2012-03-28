**With no optimalizations, refactorings and no JVM tuning using Apache ab on my MacBook after the JVM warms up for simple Rack application. **

**Absolutely unscientific.**

**The testing tool was run on the same machine as the server itself.**

The most basic test

    Concurrency Level:      1
    Time taken for tests:   0.645 seconds
    Complete requests:      1000
    Failed requests:        0
    Write errors:           0
    Total transferred:      47000 bytes
    HTML transferred:       3000 bytes
    Requests per second:    1550.47 [#/sec] (mean)
    Time per request:       0.645 [ms] (mean)
    Time per request:       0.645 [ms] (mean, across all concurrent requests)
    Transfer rate:          71.16 [Kbytes/sec] received

    Connection Times (ms)
                  min  mean[+/-sd] median   max
    Connect:        0    0   0.1      0       3
    Processing:     0    1   1.1      0      27
    Waiting:        0    0   1.0      0      27
    Total:          0    1   1.1      0      27

with concurency 10

    Concurrency Level:      10
    Time taken for tests:   3.644 seconds
    Complete requests:      10000
    Failed requests:        0
    Write errors:           0
    Total transferred:      470329 bytes
    HTML transferred:       30021 bytes
    Requests per second:    2744.33 [#/sec] (mean)
    Time per request:       3.644 [ms] (mean)
    Time per request:       0.364 [ms] (mean, across all concurrent requests)
    Transfer rate:          126.05 [Kbytes/sec] received

    Connection Times (ms)
                  min  mean[+/-sd] median   max
    Connect:        0    0   0.6      0      18
    Processing:     0    3   3.6      2      67
    Waiting:        0    3   3.5      2      67
    Total:          1    4   3.6      3      67

with concurency 100

    Concurrency Level:      100
    Time taken for tests:   0.340 seconds
    Complete requests:      1000
    Failed requests:        4
       (Connect: 4, Receive: 0, Length: 0, Exceptions: 0)
    Write errors:           0
    Total transferred:      47987 bytes
    HTML transferred:       3063 bytes
    Requests per second:    2942.32 [#/sec] (mean)
    Time per request:       33.987 [ms] (mean)
    Time per request:       0.340 [ms] (mean, across all concurrent requests)
    Transfer rate:          137.88 [Kbytes/sec] received

    Connection Times (ms)
                  min  mean[+/-sd] median   max
    Connect:        0    4   4.6      2      22
    Processing:     1   15   9.5     12      53
    Waiting:        1   12   8.4      9      49
    Total:          4   18   9.3     15      54
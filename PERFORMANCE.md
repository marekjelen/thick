**With no optimalizations, refactorings and no JVM tuning using Apache ab on my MacBook after the JVM warms up for simple Rack application. **

**Absolutely unscientific.**

**The testing tool was run on the same machine as the server itself.**

The most basic test

    Concurrency Level:      1
    Time taken for tests:   1.837 seconds
    Complete requests:      1000
    Failed requests:        0
    Write errors:           0
    Total transferred:      141000 bytes
    HTML transferred:       2000 bytes
    Requests per second:    544.36 [#/sec] (mean)
    Time per request:       1.837 [ms] (mean)
    Time per request:       1.837 [ms] (mean, across all concurrent requests)
    Transfer rate:          74.96 [Kbytes/sec] received

    Connection Times (ms)
                  min  mean[+/-sd] median   max
    Connect:        0    0   0.0      0       0
    Processing:     0    1   0.6      2      16
    Waiting:        0    1   0.6      1      16
    Total:          1    2   0.7      2      16
    WARNING: The median and mean for the processing time are not within a normal deviation
            These results are probably not that reliable.

    Percentage of the requests served within a certain time (ms)
      50%      2
      66%      2
      75%      2
      80%      2
      90%      2
      95%      2
      98%      2
      99%      3
     100%     16 (longest request)

with concurency 10

    Concurrency Level:      10
    Time taken for tests:   1.930 seconds
    Complete requests:      10000
    Failed requests:        0
    Write errors:           0
    Total transferred:      1410000 bytes
    HTML transferred:       20000 bytes
    Requests per second:    5181.86 [#/sec] (mean)
    Time per request:       1.930 [ms] (mean)
    Time per request:       0.193 [ms] (mean, across all concurrent requests)
    Transfer rate:          713.52 [Kbytes/sec] received

    Connection Times (ms)
                  min  mean[+/-sd] median   max
    Connect:        0    0   0.2      0       2
    Processing:     0    2   1.6      1      23
    Waiting:        0    2   1.5      1      23
    Total:          0    2   1.6      1      23

    Percentage of the requests served within a certain time (ms)
      50%      1
      66%      2
      75%      2
      80%      2
      90%      3
      95%      4
      98%      6
      99%      8
     100%     23 (longest request)

with concurency 100

    Concurrency Level:      100
    Time taken for tests:   1.766 seconds
    Complete requests:      10000
    Failed requests:        0
    Write errors:           0
    Total transferred:      1411128 bytes
    HTML transferred:       20016 bytes
    Requests per second:    5660.98 [#/sec] (mean)
    Time per request:       17.665 [ms] (mean)
    Time per request:       0.177 [ms] (mean, across all concurrent requests)
    Transfer rate:          780.11 [Kbytes/sec] received

    Connection Times (ms)
                  min  mean[+/-sd] median   max
    Connect:        0    2   1.9      2       9
    Processing:     0   15  11.0     12      78
    Waiting:        0   14  11.1     11      76
    Total:          0   17  10.8     15      81

    Percentage of the requests served within a certain time (ms)
      50%     15
      66%     19
      75%     22
      80%     24
      90%     32
      95%     39
      98%     48
      99%     53
     100%     81 (longest request)
# RateLimiter
In this project, I implemented two rate limiters.
One is a wrapper to Guava - open-source written by Google.
For a distributed scaling system, guava will be more efficient to use than an own written algorithm. Guava is defining the max permits per second, and in its implementation, it goes down to the milliseconds level.
The second one is a simple rate limiter, using Akka scheduler and an atomic counter that increments the counter until it reaches the max permits defined. This solution will be better in cases the number of requests isn’t big per the time we want to throttle.

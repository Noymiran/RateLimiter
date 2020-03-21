# RateLimiter
The purpose of this project is to create a rate-limiting module that stops a particular requester from making too many http requests within a particular period of time.

I defined a requester by his unique IP. If there wasn't an IP in the request header, the response will be Unauthorized (status code 401)

In this project, I implemented two rate limiters.
1. A wrapper to Guava - open-source written by Google.
For a distributed scaling system, guava will be more efficient to use than an own written algorithm. Guava is defining the max permits per second, and in its implementation, it goes down to the milliseconds level.
2. A simple rate limiter, using Akka scheduler and an atomic counter that increments the counter until it reaches the max permits defined. This solution will be better in cases the number of requests isn’t big per the time we want to throttle.


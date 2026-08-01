# frontend and backend security
how can we secure our frontend, be, db from diff attacks, read about it
how to safely share data across internet that are vulnerable like tokens and cookies etc
# media sharing to cloudinary/s3
-> how to compress images into smaller size
-> how to load image faster??

# video compression
how video compression works 

## later i will try to go backwards and watch where the painpoints are
# oauth
how to enable sign-in with google
# hld
# lld
# caching
# messaging queues

# database 
-> why to use uuid instead of id sequential in db ? benefits and disadv
    -> Advantages UUID(Universally Unique Identifier)
            1. Security & Invisibility: Prevents ID enumeration attacks (also known as the "German Tank Problem"). An attacker can't easily guess that user 100 exists just because they are user 99, or scrape all your endpoints sequentially (/api/users/1, /api/users/2).
            2. client-side gen -> uuid can be generated locally before making insert req to db, so we doesn't need to wait for db to return the last known id to increment
            3. distributed & multi-region friendly -> Multiple systems can generate IDs independently without colliding or coordinating via a single bottleneck database sequence.
    -> Disadvantages
        1. performance penality on indexes
        2. large storage footprint -> as uui takes up to 16bytes compared to 4bytes(int) or 8bytes(bigint)
        3. harder to debug 
        4. not naturally sorted -> need some timestamp to see latest record

-> letsay i am using transactional and some user clicks two times , won't idempotency break as both request is isolately running on memory for now and noone knows about each other ??
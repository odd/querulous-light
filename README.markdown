# Querulous-Light (vanilla JDBC fork)

An agreeable way to talk to your database.

Note: This is Olles fork of Querulous-generic (in turn a fork of the Twitter verion). This fork will:
* Work on Scala 2.8
* Work on other DBs than MySQL (I use H2)

## License

Copyright 2010 Twitter, Inc. See included LICENSE file.

## Features

* Handles all the JDBC bullshit so you don't have to: type casting for primitives and collections, exception handling and transactions, and so forth;
* Fault tolerant: configurable strategies such as timeouts, mark-dead thresholds, and retries;
* Designed for operability: rich statistics about your database usage and extensive debug logging;
* Minimalist: minimal code, minimal assumptions, minimal dependencies. You write highly-tuned SQL and we get out of the way;
* Highly modular, highly configurable.

The Github source repository is {here}[http://github.com/nkallen/querulous/]. Patches and contributions are  
welcome.

## Understanding the Implementation

`Querulous` is made out of three components: QueryEvaluators, Queries, and Databases.

* QueryEvaluators are a convenient procedural interface for executing queries.
* Queries are objects representing a SELECT/UPDATE/INSERT/DELETE SQL Query. They are responsible for most type-casting, timeouts, and so forth. You will rarely interact with Queries directly.
* Databases reserve and release connections an actual database.

Each of these three kinds of objects implement an interface. Enhanced functionality is meant to be "layered-on" by wrapping decorators around these objects that implement the enhanced functionality and delegate the primitive functionality.

Each of the three components are meant to be instantiated with their corresponding factories (e.g., QueryEvaluatorFactory, DatabaseFactory, etc.). The system is made configurable by constructing factories that manufacture the Decorators you're interested in. For example,

    val queryFactory = new DebuggingQueryFactory(new TimingOutQueryFactory(new SqlQueryFactory))
    val query = queryFactory(...) // this query will have debugging information and timeouts!

## Usage

### Basic Usage

Create a QueryEvaluator by connecting to a database with a JDBC driver name, JDBC url, username and password:

    import com.twitter.querulous.evaluator.QueryEvaluator
	val queryEvaluator = QueryEvaluator("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/mydb", "username", "password")

Run a query or two:

    val users = queryEvaluator.select("SELECT * FROM users WHERE id IN (?) OR name = ?", List(1,2,3), "Jacques") { row =>
      new User(row.getInt("id"), row.getString("name"))
    }
    queryEvaluator.execute("INSERT INTO users VALUES (?, ?)", 1, "Jacques")

Note that sequences are handled automatically (i.e., you only need one question-mark (?)).

Run a query in a transaction for enhanced pleasure:

    queryEvaluator.transaction { transaction =>
      transaction.select("SELECT ... FOR UPDATE", ...)
      transaction.execute("INSERT INTO users VALUES (?, ?)", 1, "Jacques")
      transaction.execute("INSERT INTO users VALUES (?, ?)", 2, "Luc")
    }

The yielded transaction object implements the same interface as QueryEvaluator. Note that the transaction will be rolled back if you raise an exception.

### Advanced Usage

For production-quality use of `Querulous` you'll want to set configuration options and layer-on more functionality. Here is the maximally configurable, if somewhat elaborate, way to instantiate a QueryEvaluator

    import com.twitter.querulous.evaluator._
    import com.twitter.querulous.query._
    import com.twitter.querulous.database._

    val queryFactory = new SqlQueryFactory
    val apachePoolingDatabaseFactory = new apachePoolingDatabaseFactory(
	  validationQuery:                    String,   // query which is executed to check health of open connections	
      minOpenConnections:                 Int,      // minimum number of open/active connections at all times
      maxOpenConnections:                 Int,      // minimum number of open/active connections at all times
      checkConnectionHealthWhenIdleFor:   Duration, // asynchronously check the health of open connections every `checkConnectionHealthWhenIdleFor` amount of time
      maxWaitForConnectionReservation:    Duration, // maximum amount of time you're willing to wait to reserve a connection from the pool; throw an exception otherwise
      checkConnectionHealthOnReservation: Boolean,  // check connection health when reserving the connection from the pool
      evictConnectionIfIdleFor:           Duration  // destroy connections if they are idle for longer than `evictConnectionIfIdleFor` amount of time
    )
    val queryEvaluatorFactory = new StandardQueryEvaluatorFactory(apachePoolingDatabaseFactory, queryFactory)
    val queryEvaluator = queryEvaluatorFactory("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/mydb", "username", "password")

Now comes the fun part.

### Recommended Configuration Options

* Set minActive equal to maxActive. This ensures that the system is fully utilizing the connection resource even when the system is idle. This is good because you will not be surprised by connection usage (and e.g., unexpectedly hit server-side connection limits) during peak load.
* Set minActive equal to maxActive equal to the MySql connection limit divided by the number of instances of your client process
* Set testIdle to 1.second or so. It should be substantially less than the server-side connection timeout.
* Set maxWait to 10.millis--to start. In general, it should be set to the average experienced latency plus twice the standard deviation. Gather statistics!
* Set minEvictableIdle to 5.minutes or more. It has no effect when minActive equals maxActive, but in case these differ you don't want excessive connection churning. It should certainly be less than or equal to the server-side connection timeout.

## Statistics Collection

StatsCollector is actually just a trait that you'll need to implement using your favorite statistics collecting library. My favorite is [Ostrich](http://github.com/robey/ostrich) and you can write an adapter in a few lines of code. Here is one such adapter:

    val stats = new StatsCollector {
      def incr(name: String, count: Int) = Stats.incr(name, count)
      def time[A](name: String)(f: => A): A = Stats.time(name)(f)
    }
    val databaseFactory = new StatsCollectingDatabaseFactory(new ApachePoolingDatabaseFactory(...), stats)

## Installation

Checkout source, run `ant`, then if all goes well, copy dist/querulous-xxxx.jar into your project's classpath.

## Running Tests

From the command line, simply run:

    % ant test

## Reporting problems

The Github issue tracker is {here}[http://github.com/nkallen/querulous/issues].

## Contributors

* Nick Kallen
* Robey Pointer
* Ed Ceaser
* Utkarsh Srivastava

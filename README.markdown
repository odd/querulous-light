# Querulous-Light (vanilla JDBC fork)

An agreeable way to talk to your database.

## Note about this Fork
This is a fork of Querulous-generic (Querulous-generic is in turn a fork of the Twitter version). This fork will:

* Work on Scala 2.8
* Work on all RDBMs (Well, I've only tried H2, so I do not really know)
* Not have any dependencies to Twitters StandardProject module.

The more advanced features (timeout and so on) probably don't work, since I had to smash some code to get here.

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

## Reporting problems

The Github issue tracker is {here}[http://github.com/nkallen/querulous/issues].

## Contributors

* Nick Kallen
* Robey Pointer
* Ed Ceaser
* Utkarsh Srivastava

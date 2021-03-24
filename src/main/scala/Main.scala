package com.albertoperez1994.scalaqlexample


object Setup {

  import java.sql.DriverManager

  val sqliteConnection = {

    Class.forName("org.sqlite.JDBC");
    DriverManager.getConnection("jdbc:sqlite::memory:")
  }

  val initializeDb = {

    val sqls = Seq( """ CREATE TABLE Person (
                      name text,
                      age int,
                      isEmployer int,
                      addressId int,
                      telephoneId int) """,

                    "CREATE TABLE Address (id int, street text)",
                    "CREATE TABLE Telephone (id int, number string)")

    val stmt = sqliteConnection.createStatement()

    for (sql <- sqls)
      stmt.addBatch(sql)

    stmt.executeBatch()
  }
}

object Application extends App {

  import com.albertoperez1994.scalaql._

  val conn = Setup.sqliteConnection


  // Database Table Models
  case class Person (name: String, age: Int, isEmployer: Boolean, addressId: Int, telephoneId: Int)
                      extends DbTable
  case class Address (id: Int, street: String) extends DbTable
  case class Telephone (id: Int, number: String) extends DbTable


  // Query Result Model
  case class Result (name: String, age: Int, street: String, telephoneNumber: String) extends DbResult



  val names = List("John", "Mark", "Thomas")
  val john = Person ("John", 50, true, 2, 1)
  val thomas = Person ("Thomas", 36, true, 2, 1)
  val address = Address (2, "Baker Street")



  val stmts = Seq(insert(Seq(john, thomas)),
                  insert(address),
                  update[Person] (p => (Map(p.name -> "Mark",
                                            p.age  -> 50),
                                        Where(p.name === "John"))),
                  delete[Address] (h => Where(h.street <> "Baker Street")))


  val qry = query[(Person, Address, Telephone)].select {
    case (p, a, t) ⇒ Query(
      Select          (Result (p.name, p.age, a.street, t.number)),
      Where           (a.street like "%Baker St%" or a.street === "",
                       p.name in names,
                       coalesce(p.isEmployer, false)),
      OrderBy         (desc (p.age)),
      LeftJoin (a)    (a.id === p.addressId),
      LeftJoin (t)    (t.id === p.telephoneId))
  }

  implicit val context = new ScalaQLContext(conn)
  context.run(stmts:_*)
  val results = qry.run()

  println(stmts.mkString("\n\n", "", ""))
  println(s"$qry \nResults: \n\n${results.mkString(", \n")} \n")
}

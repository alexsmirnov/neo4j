/**
 * Copyright (c) 2002-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.docgen

import org.junit.Test
import org.junit.Assert._

class AggregationTest extends DocumentingTestBase {
  def graphDescription = List("A:Person KNOWS B:Person", "A KNOWS C:Person", "A KNOWS D:Person")


  override val properties: Map[String, Map[String, Any]] = Map(
    "A" -> Map("property" -> 13),
    "B" -> Map("property" -> 33, "eyes" -> "blue"),
    "C" -> Map("property" -> 44, "eyes" -> "blue"),
    "D" -> Map("eyes" -> "brown")
  )

  def section = "Aggregation"

  @Test def countNodes() {
    testQuery(
      title = "Count nodes",
      text = "To count the number of nodes, for example the number of nodes connected to one node, you can use `count(*)`.",
      queryText = "match (n)-->(x) where n.name = 'A' return n, count(*)",
      returns = "This returns the start node and the count of related nodes.",
      assertions = p => assertEquals(Map("n" -> node("A"), "count(*)" -> 3), p.toList.head))
  }

  @Test def countRelationshipsByType() {
    testQuery(
      title = "Group Count Relationship Types",
      text = "To count the groups of relationship types, return the types and count them with `count(*)`.",
      queryText = "match (n)-[r]->() where n.name='A' return type(r), count(*)",
      returns = "The relationship types and their group count is returned by the query.",
      assertions = p => assertEquals(Map("type(r)" -> "KNOWS", "count(*)" -> 3), p.toList.head))
  }

  @Test def countEntities() {
    testQuery(
      title = "Count entities",
      text = "Instead of counting the number of results with `count(*)`, it might be more expressive to include " +
        "the name of the identifier you care about.",
      queryText = "match (n)-->(x) where n.name = 'A' return count(x)",
      returns = "The example query returns the number of connected nodes from the start node.",
      assertions = p => assertEquals(Map("count(x)" -> 3), p.toList.head))
  }

  @Test def countNonNullValues() {
    testQuery(
      title = "Count non-null values",
      text = "You can count the non-`null` values by using +count(<identifier>)+.",
      queryText = "match n:Person return count(n.property)",
      returns = "The count of related nodes with the `property` property set is returned by the query.",
      assertions = p => assertEquals(Map("count(n.property)" -> 3), p.toList.head))
  }

  @Test def sumProperty() {
    testQuery(
      title = "SUM",
      text = "The +SUM+ aggregation function simply sums all the numeric values it encounters. " +
        "Nulls are silently dropped. This is an example of how you can use +SUM+.",
      queryText = "match n:Person where has(n.property) return sum(n.property)",
      returns = "This returns the sum of all the values in the property `property`.",
      assertions = p => assertEquals(Map("sum(n.property)" -> (13 + 33 + 44)), p.toList.head))
  }

  @Test def avg() {
    testQuery(
      title = "AVG",
      text = "+AVG+ calculates the average of a numeric column.",
      queryText = "match n:Person where has(n.property) return avg(n.property)",
      returns = "The average of all the values in the property `property` is returned by the example query.",
      assertions = p => assertEquals(Map("avg(n.property)" -> 30), p.toList.head))
  }

  @Test def min() {
    testQuery(
      title = "MIN",
      text = "+MIN+ takes a numeric property as input, and returns the smallest value in that column.",
      queryText = "match n:Person where has(n.property) return min(n.property)",
      returns = "This returns the smallest of all the values in the property `property`.",
      assertions = p => assertEquals(Map("min(n.property)" -> 13), p.toList.head))
  }

  @Test def max() {
    testQuery(
      title = "MAX",
      text = "+MAX+ find the largets value in a numeric column.",
      queryText = "match n:Person where has(n.property) return max(n.property)",
      returns = "The largest of all the values in the property `property` is returned.",
      assertions = p => assertEquals(Map("max(n.property)" -> 44), p.toList.head))
  }

  @Test def collect() {
    testQuery(
      title = "COLLECT",
      text = "+COLLECT+ collects all the values into a list. It will ignore null values,",
      queryText = "match n:Person return collect(n.property)",
      returns = "Returns a single row, with all the values collected.",
      assertions = p => assertEquals(Map("collect(n.property)" -> Seq(13, 33, 44)), p.toList.head))
  }

  @Test def count_distinct() {
    testQuery(
      title = "DISTINCT",
      text = """All aggregation functions also take the +DISTINCT+ modifier, which removes duplicates from the values.
So, to count the number of unique eye colors from nodes related to `a`, this query can be used: """,
      queryText = "match a:Person-->b where a.name = 'A' return count(distinct b.eyes)",
      returns = "Returns the number of eye colors.",
      assertions = p => assertEquals(Map("count(distinct b.eyes)" -> 2), p.toList.head))
  }
  
  @Test def intro() {
    testQuery(
      title = "Introduction",
      text = """To calculate aggregated data, Cypher offers aggregation, much like SQL's +GROUP BY+.

Aggregate functions take multiple input values and calculate an aggregated value from them. Examples are +AVG+ that
calculate the average of multiple numeric values, or +MIN+ that finds the smallest numeric value in a set of values.

Aggregation can be done over all the matching sub graphs, or it can be further divided by introducing key values.
These are non-aggregate expressions, that are used to group the values going into the aggregate functions.

So, if the return statement looks something like this:

[source,cypher]
----
RETURN n, count(*)
----

We have two return expressions -- `n`, and `count(*)`. The first, `n`, is no aggregate function, and so it will be the
grouping key. The latter, `count(*)` is an aggregate expression. So the matching subgraphs will be divided into
different buckets, depending on the grouping key. The aggregate function will then run on these buckets, calculating
the aggregate values.

If you want to use aggregations to sort your result set, the aggregation must be included in the +RETURN+ to be used
in your +ORDER BY+.

The last piece of the puzzle is the +DISTINCT+ keyword. It is used to make all values unique before running them through
an aggregate function.

An example might be helpful:""",
      queryText = "" +
      		"MATCH me:Person-->friend:Person-->friend_of_friend:Person " +
          "WHERE me.name = 'A'" +
      		"RETURN count(distinct friend_of_friend), count(friend_of_friend)",
      returns = "In this example we are trying to find all our friends of friends, and count them. The first aggregate function, " +
      		"+count(distinct friend_of_friend)+, will only see a `friend_of_friend` once -- +DISTINCT+ removes the duplicates. The latter " +
      		"aggregate function, +count(friend_of_friend)+, might very well see the same `friend_of_friend` multiple times. Since there is " +
      		"no real data in this case, an empty result is returned. See the sections below for real data.",
      assertions = p => assertTrue(true))
  }

  @Test def percentile_disc() {
    testQuery(
      title = "PERCENTILE_DISC",
      text = "+PERCENTILE_DISC+ calculates the percentile of a given value over a group, with a percentile from 0.0 to 1.0. It uses a rounding method, returning the nearest value to the percentile. For interpolated values, see PERCENTILE_CONT.",
      queryText = "match n:Person where has(n.property) return percentile_disc(n.property, 0.5)",
      returns = "The 50th percentile of the values in the property `property` is returned by the example query. In this case, 0.5 is the median, or 50th percentile.",
      assertions = p => assertEquals(Map("percentile_disc(n.property, 0.5)" -> 33), p.toList.head))
  }

  @Test def percentile_cont() {
    testQuery(
      title = "PERCENTILE_CONT",
      text = "+PERCENTILE_CONT+ calculates the percentile of a given value over a group, with a percentile from 0.0 to 1.0. It uses a linear interpolation method, calculating a weighted average between two values, if the desired percentile lies between them. For nearest values using a rounding method, see PERCENTILE_DISC.",
      queryText = "match n:Person where has(n.property) return percentile_cont(n.property, 0.4)",
      returns = "The 40th percentile of the values in the property `property` is returned by the example query, calculated with a weighted average.",
      assertions = p => assertEquals(Map("percentile_cont(n.property, 0.4)" -> 29), p.toList.head))
  }

  @Test def stdev() {
    testQuery(
      title = "STDEV",
      text = "+STDEV+ calculates the standard deviation for a given value over a group. It uses a standard two-pass method, with N-1 as the denominator, and should be used when taking a sample of the population for an unbiased estimate. When the standard variation of the entire population is being calculated, STDEVP should be used.",
      queryText = "start n=node(%A%,%B%,%C%) return stdev(n.property)",
      returns = "The standard deviation of the values in the property `property` is returned by the example query.",
      assertions = p => assertEquals(15.7162336455,p.toList.head("stdev(n.property)").asInstanceOf[Double], 0.0000001))
  }

  @Test def stdevp() {
    testQuery(
      title = "STDEVP",
      text = "+STDEVP+ calculates the standard deviation for a given value over a group. It uses a standard two-pass method, with N as the denominator, and should be used when calculating the standard deviation for an entire population. When the standard variation of only a sample of the population is being calculated, STDEV should be used.",
      queryText = "start n=node(%A%,%B%,%C%) return stdevp(n.property)",
      returns = "The population standard deviation of the values in the property `property` is returned by the example query.",
      assertions = p => assertEquals(12.8322510366, p.toList.head("stdevp(n.property)").asInstanceOf[Double], 0.0000001))
  }

}

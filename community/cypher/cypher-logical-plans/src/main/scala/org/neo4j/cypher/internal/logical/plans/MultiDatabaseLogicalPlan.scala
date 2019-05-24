/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
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
package org.neo4j.cypher.internal.logical.plans

import org.neo4j.cypher.internal.ir.{LazyMode, StrictnessMode}
import org.neo4j.cypher.internal.v4_0.ast.{ActionResource, GraphScope, PrivilegeQualifier, ShowPrivilegeScope}
import org.neo4j.cypher.internal.v4_0.expressions.Parameter
import org.neo4j.cypher.internal.v4_0.util.CypherException
import org.neo4j.cypher.internal.v4_0.util.attribution.IdGen
import org.neo4j.cypher.internal.v4_0.util.spi.MapToPublicExceptions

class DatabaseManagementException(message: String) extends CypherException {
  override def mapToPublic[T <: Throwable](mapper: MapToPublicExceptions[T]): T =
    mapper.databaseManagementException(message)
}

class SecurityManagementException(message: String) extends CypherException {
  override def mapToPublic[T <: Throwable](mapper: MapToPublicExceptions[T]): T =
    mapper.securityManagementException(message)
}


abstract class MultiDatabaseLogicalPlan(idGen: IdGen) extends LogicalPlan(idGen) {
  override def lhs: Option[LogicalPlan] = None

  override def rhs: Option[LogicalPlan] = None

  override val availableSymbols: Set[String] = Set.empty

  override def strictness: StrictnessMode = LazyMode

  def invalid(message: String): RuntimeException
}

abstract class DatabaseManagementLogicalPlan(idGen: IdGen) extends MultiDatabaseLogicalPlan(idGen) {
  override def invalid(message: String): DatabaseManagementException = new DatabaseManagementException(message)
}

abstract class SecurityManagementLogicalPlan(idGen: IdGen) extends MultiDatabaseLogicalPlan(idGen) {
  override def invalid(message: String): SecurityManagementException = new SecurityManagementException(message)
}

// Security management commands
case class ShowUsers()(implicit idGen: IdGen) extends SecurityManagementLogicalPlan(idGen)
case class CreateUser(userName: String, initialStringPassword: Option[String], initialParameterPassword: Option[Parameter],
                      requirePasswordChange: Boolean, suspended: Boolean)(implicit idGen: IdGen) extends SecurityManagementLogicalPlan(idGen)
case class DropUser(userName: String)(implicit idGen: IdGen) extends SecurityManagementLogicalPlan(idGen)
case class AlterUser(userName: String, initialStringPassword: Option[String], initialParameterPassword: Option[Parameter],
                     requirePasswordChange: Option[Boolean], suspended: Option[Boolean])(implicit idGen: IdGen) extends SecurityManagementLogicalPlan(idGen)
case class ShowRoles(withUsers: Boolean, showAll: Boolean)(implicit idGen: IdGen) extends SecurityManagementLogicalPlan(idGen)
case class CreateRole(roleName: String, from: Option[String])(implicit idGen: IdGen) extends SecurityManagementLogicalPlan(idGen)
case class DropRole(roleName: String)(implicit idGen: IdGen) extends SecurityManagementLogicalPlan(idGen)
case class GrantRolesToUsers(roleNames: Seq[String], userNames: Seq[String])(implicit idGen: IdGen) extends SecurityManagementLogicalPlan(idGen)
case class RevokeRolesFromUsers(roleNames: Seq[String], userNames: Seq[String])(implicit idGen: IdGen) extends SecurityManagementLogicalPlan(idGen)
case class GrantTraverse(database: GraphScope, qualifier: PrivilegeQualifier, roleName: String)(implicit idGen: IdGen) extends SecurityManagementLogicalPlan(idGen)
case class RevokeTraverse(database: GraphScope, qualifier: PrivilegeQualifier, roleName: String)(implicit idGen: IdGen) extends SecurityManagementLogicalPlan(idGen)
case class GrantRead(resource: ActionResource, database: GraphScope, qualifier: PrivilegeQualifier, roleName: String)(implicit idGen: IdGen) extends SecurityManagementLogicalPlan(idGen)
case class RevokeRead(resource: ActionResource, database: GraphScope, qualifier: PrivilegeQualifier, roleName: String)(implicit idGen: IdGen) extends SecurityManagementLogicalPlan(idGen)
case class ShowPrivileges(scope: ShowPrivilegeScope)(implicit idGen: IdGen) extends SecurityManagementLogicalPlan(idGen)

// Database management commands
case class ShowDatabases()(implicit idGen: IdGen) extends DatabaseManagementLogicalPlan(idGen)
case class ShowDatabase(dbName: String)(implicit idGen: IdGen) extends DatabaseManagementLogicalPlan(idGen)
case class CreateDatabase(dbName: String)(implicit idGen: IdGen) extends DatabaseManagementLogicalPlan(idGen)
case class DropDatabase(dbName: String)(implicit idGen: IdGen) extends DatabaseManagementLogicalPlan(idGen)
case class StartDatabase(dbName: String)(implicit idGen: IdGen) extends DatabaseManagementLogicalPlan(idGen)
case class StopDatabase(dbName: String)(implicit idGen: IdGen) extends DatabaseManagementLogicalPlan(idGen)
package com.mardaunt.base

import slick.jdbc.PostgresProfile.api._

object Base {

  private val db = Database.forConfig("mydb")

  def getDatabase:Database = db

}

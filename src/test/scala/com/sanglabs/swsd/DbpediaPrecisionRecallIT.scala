package com.sanglabs.swsd

import org.junit.Test
import org.scalatest.Matchers
import org.scalatest.junit.{AssertionsForJUnit, JUnitSuite}

/**
 *
 * The DbpediaPrecisionRecallIT
 *
 * @author Sang Venkatraman
 *
 */
class DbpediaPrecisionRecallIT extends JUnitSuite with Matchers with AssertionsForJUnit {

  //TODO read this from a csv/excel file
  val TalkingHeads_HereWeStand =
    """
      |Here we stand
      |Like an Adam and an Eve
      |Waterfalls
      |The Garden of Eden
      |Two fools in love
      |So beautiful and strong
      |The birds in the trees
      |Are smiling upon them
      |From the age of the dinosaurs
      |Cars have run on gasoline
      |Where, where have they gone?
      |Now, it's nothing but flowers
      |
      |There was a factory
      |Now there are mountains and rivers
      |you got it, you got it
      |
      |We caught a rattlesnake
      |Now we got something for dinner
      |we got it, we got it
      |
      |There was a shopping mall
      |Now it's all covered with flowers
      |you've got it, you've got it
      |
      |If this is paradise
      |I wish I had a lawnmower
      |you've got it, you've got it
      |
      |Years ago
      |I was an angry young man
      |I'd pretend
      |That I was a billboard
      |Standing tall
      |By the side of the road
      |I fell in love
      |With a beautiful highway
      |This used to be real estate
      |Now it's only fields and trees
      |Where, where is the town
      |Now, it's nothing but flowers
      |The highways and cars
      |Were sacrificed for agriculture
      |I thought that we'd start over
      |But I guess I was wrong
      |
      |Once there were parking lots
      |Now it's a peaceful oasis
      |you got it, you got it
      |
      |This was a Pizza Hut
      |Now it's all covered with daisies
      |you got it, you got it
      |
      |I miss the honky tonks,
      |Dairy Queens, and 7-Elevens
      |you got it, you got it
      |
      |And as things fell apart
      |Nobody paid much attention
      |you got it, you got it
      |
      |I dream of cherry pies,
      |Candy bars, and chocolate chip cookies
      |you got it, you got it
      |
      |We used to microwave
      |Now we just eat nuts and berries
      |you got it, you got it
      |
      |This was a discount store,
      |Now it's turned into a cornfield
      |you got it, you got it
      |
      |Don't leave me stranded here
      |I can't get used to this lifestyle
    """.stripMargin

  val PaulSimon_Kodachrome =
    """
      |When I think back
      |On all the crap I learned in high school
      |It's a wonder
      |I can think at all
      |And though my lack of education
      |Hasn't hurt me none
      |I can read the writing on the wall
      |
      |Kodachrome
      |They give us those nice bright colors
      |They give us the greens of summers
      |Makes you think all the world's
      |a sunny day
      |I got a Nikon camera
      |I love to take a photograph
      |So mama don't take my Kodachrome away
      |
      |If you took all the girls I knew
      |When I was single
      |And brought them all together
      |for one night
      |I know they'd never match
      |my sweet imagination
      |Everything looks worse
      |in black and white
    """.stripMargin

  val TheCrash_BrandNewCadillac =
    """
      |Driiiiiiiive!!!
      |Driiiiiiiive!!!
      |
      |My baby drove up in a brand new Cadillac
      |Yes she did!
      |My baby drove up in a brand new Cadillac
      |She said, "Hey, come here, Daddy!"
      |"I ain't never comin' back!"
      |
      |Baby, baby, won't you hear my plea?
      |C'mon, sugar, just come on back to me
      |She said, "Balls to you, Big Daddy."
      |She ain't never coming back!
      |
      |Baby, baby, won't you hear my plea?
      |Oh c'mon, just hear my plea
      |She said, "Balls to you, Daddy."
      |She ain't coming back to me
      |
      |Baby, baby drove up in a Cadillac
      |I said, "Jesus Christ! Where'd you get that cadillac?"
      |She said, "Balls to you, Daddy."
      |She ain't never coming back!
    """.stripMargin

  val ArchticMonkeys_ACertainRomance =
    """
      |Well oh they might wear classic Reeboks
      |Or knackered Converse
      |Or tracky bottoms tucked in socks
      |But all of that's what the point is not
      |The point is that there ain't no romance around there
      |
      |And there's the truth that they can't see
      |They'd probably like to throw a punch at me
      |And if you could only see them, then you would agree
      |Agree that there ain't no romance around there
      |
      |You know, oh, it's a funny thing, you know?
      |We'll tell them if you like
      |We'll tell them all tonight
      |They'll never listen
      |Because their minds are made up
      |And course it's all OK to carry on that way
      |
      |'Cause over there there's broken bones
      |There's only music, so that there's new ringtones
      |And it don't take no Sherlock Holmes
      |To see it's a little different around here
      |
      |Don't get me wrong though there's boys in bands
      |And kids who like to scrap with pool cues in their hands
      |And just 'cause he's had a couple of cans
      |He thinks it's alright to act like a dickhead
      |
      |Don't you know, oh it's a funny thing you know
      |We'll tell them if you like
      |We'll tell them all tonight
      |They'll never listen
      |'Cause their minds are made up
      |And course is all OK to carry on that way
      |But I said no, oh no!
      |Well oh, you won't get me to go
      |Not anywhere, not anywhere
      |No, I won't go, oh no, no?
      |
      |But over there there's friends of mine
      |What can I say, I've known them for a long long time
      |And yet they might overstep the line
      |But you just cannot get angry in the same way
      |No, not in the same way
      |No not in the same way
      |Oh no, oh no, no!
    """.stripMargin

  val MyHumps_BlackEyedPeas = """I'm so official ya'll
                                |You can check my record.
                                |My dress code is elevated,
                                |No one can do it better.
                                |What kind of chick you know rock Louis from head to toe?
                                |I'm Incredible.
                                |So ATL what you know about me?
                                |Frankie B jeans Jimmy Choos on my feet.
                                |I walk up in the store they already know my name.
                                |Now go into the back and give me one of errthang.
                                |
                                |Gucci this, Gucci that
                                |Gucci everything [x3]
                                |Louis this, Louis that
                                |Louis errthang [x3]
                                |Give me this, give me that
                                |Give me errthang [x3]
                                |Killing this, killing that
                                |Killing errthang [x2]
                                |
                                |On my pretty hustle with the fashion,
                                |The way I put my fists together.
                                |Fendi bag compliment the Prada shoes and patten leather.
                                |What kind of chick you know rocks Gucci from head to toe?
                                |I'm impeccable!
                                |See you might have the same outfit.
                                |If you ain't got my swagg you can't rock it like this.
                                |I'm a fashionista when it comes to doing this.
                                |My taste is so exclusive and it's so expensive!
                                |Oh!
                                |
                                |Gucci this, Gucci that
                                |Gucci everything [x3]
                                |Louis this, Louis that
                                |Louis errthang [x3]
                                |Give me this, give me that
                                |Give me errthang [x3]
                                |Killing this, killing that
                                |Killing everything [x2]
                                |That's on errthang
                                |
                                |When I walk into the store, I know what I want.
                                |Want one of everything I don't care what it cost.
                                |The hats, the scarfs, the shirt, the pants.
                                |Unlock the case of the sunglasses.
                                |Gotta, gotta make sure my bag matches.
                                |My accessories gotta be flashy
                                |Especially cause I'm classy
                                |Babydoll, and my swagg sassy.
                                |OMG we so live,
                                |This is how we arrive.
                                |My sneaker game is dynamite.
                                |The young diva leggoh we ride
                                |Beauty, Star and you got me my Betsy Johnson jewelry.
                                |Three of the cutest things you ever seen!
                                |OMG on errthang.
                                |
                                |Gucci this, Gucci that
                                |Gucci errthang [x3]
                                |Louis this, Louis that
                                |Louis errthang [x3]
                                |Give me this, give me that
                                |Give me errthang [x3]
                                |Killing this, killing that
                                |Killing everything [x2]
                                |That's on errthang!"""

  val textWithExpectedEntities : List[(String,Array[String])] = List((TalkingHeads_HereWeStand,Array("http://dbpedia.org/resource/Pizza_Hut","http://dbpedia.org/resource/7-Eleven","http://dbpedia.org/resource/Dairy_Queen")),
    (PaulSimon_Kodachrome,Array("http://dbpedia.org/resource/Kodachrome","http://dbpedia.org/resource/Nikon")),
    (TheCrash_BrandNewCadillac,Array("http://dbpedia.org/resource/Cadillac")),
    (ArchticMonkeys_ACertainRomance,Array("http://dbpedia.org/resource/Reebok","http://dbpedia.org/resource/Converse_(shoe_company)")),
    (MyHumps_BlackEyedPeas,Array("http://dbpedia.org/resource/Prada","http://dbpedia.org/resource/Gucci","http://dbpedia.org/resource/Fendi","http://dbpedia.org/resource/Dolce_&_Gabbana"," http://dbpedia.org/resource/True_Religion","http://dbpedia.org/resource/Cocoa_Puff")))


  @Test
  def testAvgFMeasureFromDbpedia() = {
    val namedEntities = textWithExpectedEntities.map(t => DbpediaSpotlightService.getEntities(t._1))
    val expectedAndPredictedResults = for(i <- 0 until textWithExpectedEntities.length) yield (textWithExpectedEntities(i)._2.map(_.asInstanceOf[AnyRef]),namedEntities(i).map(_.uri.asInstanceOf[AnyRef]).toArray)
    val fMeasure = FMeasureCalculator.avgFMeasure(expectedAndPredictedResults.toList)
    assert(fMeasure.fMeasure > 0.73 && fMeasure.fMeasure < 0.75)
    println(fMeasure)
  }


}

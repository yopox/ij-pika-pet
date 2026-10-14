package fr.yopox.pika

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project

fun registerAnimationTriggers(petService: PikaPetService) {
    val project = petService.project
    dumbModeTrigger(project, petService)
}

private fun dumbModeTrigger(project: Project, petService: PikaPetService) {
    project.messageBus.connect(petService).subscribe(DumbService.DUMB_MODE, object : DumbService.DumbModeListener {
        override fun enteredDumbMode() {
            petService.playAnimation(PikaAnimation.WAIT)
        }
        override fun exitDumbMode() {
            petService.playOneShot(PikaAnimation.CELEBRATE, loops = 2)
        }
    })
}
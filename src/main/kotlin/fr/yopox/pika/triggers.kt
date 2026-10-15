package fr.yopox.pika

import com.intellij.analysis.problemsView.Problem
import com.intellij.analysis.problemsView.ProblemsListener
import com.intellij.codeInsight.daemon.impl.EditorTrackerListener
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actions.BackspaceAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ex.WindowManagerListener
import com.intellij.refactoring.listeners.RefactoringEventData
import com.intellij.refactoring.listeners.RefactoringEventListener

fun registerAnimationTriggers(petService: PikaPetService) {
    val project = petService.project
    refactoringTrigger(project, petService)
    dumbModeTrigger(project, petService)
    frameTrigger(project, petService)
    problemsTrigger(project, petService)
    editorTrigger(project, petService)
    actionTrigger(project, petService)
}

fun actionTrigger(project: Project, petService: PikaPetService) {
    project.messageBus.connect(petService).subscribe(AnActionListener.TOPIC, 
        object: AnActionListener {
            override fun afterEditorTyping(c: Char, dataContext: DataContext) {
                petService.playOneShot(PikaAnimation.LOOKING, loops = 2)
            }

            override fun beforeActionPerformed(action: AnAction, event: AnActionEvent) {
                if (action is BackspaceAction) {
                    petService.playOneShot(PikaAnimation.DEPRESSED)
                }
            }
        }
    )
}

fun editorTrigger(project: Project, petService: PikaPetService) {
    project.messageBus.connect(petService).subscribe(EditorTrackerListener.TOPIC,
        object: EditorTrackerListener {
            override fun activeEditorsChanged(activeEditors: List<Editor>) {
            }
        }
    )
}

fun problemsTrigger(project: Project, petService: PikaPetService) {
    project.messageBus.connect(petService).subscribe(ProblemsListener.TOPIC,
        object: ProblemsListener {
            override fun problemAppeared(problem: Problem) {
            }

            override fun problemDisappeared(problem: Problem) {
            }

            override fun problemUpdated(problem: Problem) {
            }
        })
}

fun frameTrigger(project: Project, petService: PikaPetService) {
    project.messageBus.connect(petService).subscribe(WindowManagerListener.TOPIC,
        object: WindowManagerListener {
            override fun onFramesChanged() {
            
            }
        }
    )
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

fun refactoringTrigger(project: Project, petService: PikaPetService) {
    project.messageBus.connect(petService).subscribe(RefactoringEventListener.REFACTORING_EVENT_TOPIC,
        object : RefactoringEventListener {
            override fun refactoringStarted(refactoringId: String, beforeData: RefactoringEventData?) {
                petService.playAnimation(PikaAnimation.HEARTS_2)
            }

            override fun refactoringDone(p0: String, p1: RefactoringEventData?) {
                petService.playOneShot(PikaAnimation.HEARTS_3)
            }

            override fun conflictsDetected(refactoringId: String, conflictsData: RefactoringEventData) {
                petService.playOneShot(PikaAnimation.SHOCKED)
            }
        }
    )
}

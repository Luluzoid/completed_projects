#############################################################
# Caller of the functions
#
# Author: Y. Zhong 
#############################################################


#############################################################
# Data segment
#############################################################

.data
.globl INVALIDCODE
	INVALIDCODE: .word 0xFFFFFFFF

.globl NOTUSEDCODE
	NOTUSEDCODE: .word 32
	
	INPUTMSG: .asciiz "Enter a MIPS machine word in hex: 0x"
	OUTPUTMSG: .asciiz "Input: "
	INSTCODE: .asciiz "Instruction Code: "
	SRCREGS: .asciiz "Source Register(s): "
	NEXTPC: .asciiz "Next PC(s): "
	INVALID: .asciiz "Invalid"
	NOTUSED: .asciiz "N/A"
	STEPMSG: .asciiz "========================\nStep Completed: "
	
	EQUALS: .asciiz " = "
	COMMA: .asciiz ", "
	SPACE: .asciiz " "
	NEWLINE: .asciiz "\n"

.globl ZERO
	ZERO: .asciiz "0"
.globl TEN
	TEN: .asciiz "A"

	.align 4
	INPUT: .space 9

#############################################################
# Code segment
#############################################################

.text

#############################################################
# main
#############################################################
	
main:
######### get input ###########
	
	la $a0, INPUTMSG
	li $v0, 4
	syscall	# print out MSG asking for one MIPS machine code
	
	la $a0, INPUT
	li $a1, 9 
	li $v0, 8
	syscall # read in one string and store in INPUT
		
######### Step 1: atoi ##########
	#call atoi 
	la $a0, INPUT
	jal atoi

	#grab return, save returned machine code	
	add $s0, $v0, $0	#s0 has machine code

	# print out return of atoi (as hex)
	la $a0, OUTPUTMSG
	li $v0, 4
	syscall
	add $a0, $s0, $0
	li $v0, 34
	syscall
	jal print_newline
	
######## end of Step 1 #################
########################################	
	
########## Step 2: get_insn_code #######
	#call get_insn_code
	add $a0, $s0, $0
	jal get_insn_code
	add $s1, $v0, $0	
	
	# print out return of get_inst_code (as decimal)	
	la $a0, INSTCODE
	li $v0, 4
	syscall
	
	lw $t0, INVALIDCODE
	bne $s1, $t0, insn_print
	jal print_invalid
	j end_print_insn
	
insn_print:
	add $a0, $s1, $0
	li $v0, 36
	syscall
end_print_insn:
	jal print_newline

######## end of Step 2 #################
########################################	
	
	
########## Step 3: get_src_regs ########
	# call get_src_regs
	add $a0, $s0, $0
	jal get_src_regs
	add $s1, $v0, $0
	
	# print out return of get_src_regs (as decimal)	
	la $a0, SRCREGS
	li $v0, 4
	syscall

	lw $s2, INVALIDCODE
	bne $s1, $s2, src_next
	jal print_invalid
	j end_print_src
src_next:
	lw $s2, NOTUSEDCODE
	bne $s1, $s2, print_src1
	jal print_notused
	j end_print_src

print_src1:		 
	add $a0, $s1, $0
	li $v0, 36
	syscall
	
	bne $v1, $s2, print_src2
	j end_print_src

print_src2:
	la $a0, COMMA
	li $v0, 4
	syscall	
	add $a0, $v1, $0
	li $v0, 36
	syscall		

end_print_src:	
	jal print_newline	

######## end of Step 3 #################
########################################	

########## Step 4: get_next_pc ########
	# call get_next_pc
	add $a0, $s0, $0
	add $a1, $0, $0  
	jal get_next_pc
	add $s1, $v0, $0
	add $s3, $v1, $0
	
	# print out return of get_next_pc (as hexadecimal)	
	la $a0, NEXTPC
	li $v0, 4
	syscall

	lw $s2, INVALIDCODE
	bne $s1, $s2, pc_next
	jal print_invalid
	j end_print_pc
pc_next:
	lw $s2, INVALIDCODE
	bne $s1, $s2, print_pc1
	jal print_notused
	j end_print_pc

print_pc1:		 
	add $a0, $s1, $0
	li $v0, 34
	syscall
	
	bne $s3, $s2, print_pc2
	j end_print_pc

print_pc2:
	la $a0, COMMA
	li $v0, 4
	syscall	
	add $a0, $s3, $0
	li $v0, 34
	syscall		

end_print_pc:	
	jal print_newline	

######## end of Step 4 #################
########################################	
	
exit:
	li $v0, 10
	syscall

######## end of main ###################
########################################	

#############################################################
# global subroutine: step
#############################################################
		
.globl step
step:
	add $t1, $a0, 0
	
	la $a0, STEPMSG
	li $v0, 4
	syscall	# print out STEPMSG 
	add $a0, $t1, $0
	li $v0, 36
	syscall
	la $a0, NEWLINE
	li $v0, 4
	syscall	
	
	jr $ra


#############################################################
# subroutine: print_invalid
#############################################################

print_invalid:
	la $a0, INVALID
	li $v0, 4
	syscall	
	jr $ra

#############################################################
# subroutine: print_newline
#############################################################
	
print_newline:
	la $a0, NEWLINE
	li $v0, 4
	syscall	
	jr $ra

#############################################################
# subroutine: print_notused
#############################################################

print_notused:
	la $a0, NOTUSED
	li $v0, 4
	syscall	
	jr $ra

	

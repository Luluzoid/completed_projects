#############################################################
# Callee of of decoder
#
# Author: D. Korennoi
#############################################################
# Data segment
#############################################################

.data
	NEWLINE: .asciiz "\n"

#############################################################
# Code segment
#############################################################

.text

#############################################################
# atoi
#############################################################
#############################################################
# Description:
# Writes step 1 since atoi is the first step.
# Accepts a starting address of a string buffer of an 8 character hexadecimal string
# Converts the following string into an unsigned 32-int integer
# Returns that integer
#############################################################
		
.globl atoi
atoi:
	addi $sp, $sp, -20 # allocating space on stack and pushing the registers we are going to use
	sw $t3, 16($sp)
	sw $t2, 12($sp)
	sw $t1, 8($sp)
	sw $t0, 4($sp)
	sw $ra, 0($sp) # very important to push $ra since the "jal step" instruction will overwrite it
	
#-----------------------------------
	la $t0, ($a0)
	
	add $t2, $0, $0 # extracted numeric value register
	addi $t3, $0, 268435456 # 16^7   (8th hex digit)
	
loop_string:
	lb $t1, 0($t0)  # dereferencing the input address and accesing the current character
	beq $t1, $0, done
	
	addi $t1, $t1, -48  # characters '1' to '9' are now in integer form
	bgt $t1, 9, stage_two # only jump if its a valid 'A' to 'F' character
computation:
	mul $t1, $t1, $t3  # hex digit * $t3 (16^(8-current_hex_digit))
	
	srl $t3, $t3, 4  # reducing exponent of base 16 by 1
	
	add $t2, $t2, $t1  # total value + what we got from the current hex digit
	addi $t0, $t0, 1  # increment the string to next character
	j loop_string  # repeat
stage_two: 
	addi $t1, $t1, -7 # capital letters A - F have values of their char - 55, but since we already subtracted 48, we just sub additional 7
	j computation
#-----------------------------------	
done:
	la $a0, NEWLINE # prints a newline character because it doesn't print it the first time
	li $v0, 4
	syscall	
	addi $a0, $0, 1 # passes 1 as argument for step()
	jal step
	add $v0, $t2, $zero # puts calculated integer into the return register
	lw $ra, 0($sp)
	lw $t0, 4($sp)
	lw $t1, 8($sp)
	lw $t2, 12($sp)
	lw $t3, 16($sp)
	addi $sp, $sp, 20 # popping registers off the stack and reducing the stack
	jr $ra


#############################################################
# get_insn_code
#############################################################
#############################################################
# Description:
# Writes step 2 since get_insn_code is the second step.
# Accepts an unsigned integer instruction representing the MIPS machine word.
# Extracts the opcode bits from the instruction to determine which instruction it is.
# If the opcode is 0 (two possible instructions: sub or slt), then extracts func bits since those are unique.
# If the instruction is not supported, prints that it is invalid.
# Returns a unique instruction code associated with the passed in instruction. Invalid instructions return 0xFFFFFFFF
#############################################################

.globl get_insn_code
get_insn_code:

	addi $sp, $sp, -20 # allocating space on stack and pushing the registers we are going to use
	sw $t3, 16($sp)
	sw $t2, 12($sp)
	sw $t1, 8($sp)
	sw $t0, 4($sp)
	sw $ra, 0($sp) # very important to push $ra since the "jal step" instruction will overwrite it

	add $t0, $a0, $0
	lui $t1, 0xFC00 # bitmask for the opcode section (1s in last 6 bits, 0 elsewhere)
	and $t2, $t0, $t1 # get the bits from the machine word
	srl $t2, $t2, 26 # get the actual value in the opcode section
	
	# jumps to certain labels depending on what opcode is associated with what instruction:
	beq $t2, 0x0, sub_or_slt
	beq $t2, 0x8, addi_op
	beq $t2, 0x23, lw_op
	beq $t2, 0x2b, sw_op
	beq $t2, 0x5, bne_op
	beq $t2, 0x2, j_op
	beq $t2, 0x3, jal_op 
	
	addi $t3, $0, 0xFFFFFFFF #if reaches here, then the instruction is not supported - invalid
	j got_the_insn_code

# since sub and slt share an opcode, we need this secondary label to determinte which one it is by looking at the func bits since those are different
sub_or_slt:
	and $t1, $t0, 0x3F # bitmask for the func section (first 6 bits 1s, 0 elsewhere)
	beq $t1, 0x22, sub_op # looks at func bits to determine if the instruction is sub or slt
	beq $t1, 0x2A, slt_op
	addi $t3, $0, 0xFFFFFFFF # if reaches here, invalid
	j got_the_insn_code
	
sub_op:
	addi $t3, $0, 0 # insn_code for sub is 0
	j got_the_insn_code
slt_op:
	addi $t3, $0, 2 # insn_code for slt is 2
	j got_the_insn_code
	
addi_op:
	addi $t3, $0, 1 # insn_code for addi is 1
	j got_the_insn_code

lw_op:
	addi $t3, $0, 3 # insn_code for lw is 3
	j got_the_insn_code

sw_op:
	addi $t3, $0, 4 # insn_code for sw is 4
	j got_the_insn_code

bne_op:
	addi $t3, $0, 5 # insn_code for bne is 5
	j got_the_insn_code

j_op:
	addi $t3, $0, 6 # insn_code for j is 6
	j got_the_insn_code

jal_op:
	addi $t3, $0, 7 # insn_code for jal is 7
	j got_the_insn_code


got_the_insn_code:
	addi $a0, $0, 2 # passes 2 as argument for step()
	jal step
	add $v0, $t3, $0 # calculated insn_code is put into the return register
	lw $ra, 0($sp)
	lw $t0, 4($sp)
	lw $t1, 8($sp)
	lw $t2, 12($sp)
	lw $t3, 16($sp)
	addi $sp, $sp, 20 # popping registers off the stack and reducing the stack
	jr $ra



#############################################################
# get_src_regs
#############################################################
#############################################################
# Description:
# Writes step 3 since get_src_regs is the third step.
# Accepts an unsigned integer instruction representing the MIPS machine word.
# Extracts the rs and/or rt bits from the instruction to determine if the instruction has no source, one source, or two source registers
# Valid register numbers are 0-31.
# If it is no source, $v0 is set to 32
# If only one source, $v1 is set to 32 and $v0 is set to the extracted rs bits
# If two source, $v0 is set to the extracted rs bits and $v1 is set to the extracted rt bits
# Returns the final register number(s) associated with the instruction. Invalid instructions return 0xFFFFFFFF
#############################################################

.globl get_src_regs
get_src_regs:
	addi $sp, $sp, -24 # allocating space on stack and pushing the registers we are going to use
	sw $t4, 20($sp)
	sw $t3, 16($sp)
	sw $t2, 12($sp)
	sw $t1, 8($sp)
	sw $t0, 4($sp)
	sw $ra, 0($sp)  # very important to push $ra since the "jal step" instruction will overwrite it

	add $t0, $a0, $0
	lui $t1, 0xFC00 # bitmask for the opcode section (1s in last 6 bits, 0 elsewhere)
	and $t2, $t0, $t1 
	srl $t2, $t2, 26 # getting actual value of opcode section 
	
	# since each supported instruction is either no source, one source, or two source registers, we will order them in that way
	beq $t2, 0x2, no_source
	beq $t2, 0x3, no_source
	beq $t2, 0x0, two_source
	beq $t2, 0x8, one_source
	beq $t2, 0x23, one_source
	beq $t2, 0x2b, one_source
	beq $t2, 0x5, two_source
	addi $t3, $0, 0xFFFFFFFF # if reaches here, the insturction is not supported ---> invalid
	j got_regs
	
no_source:
	addi $t3, $0, 32 # if no source, $v0 will be set to 32 (since the supported range is 0 - 31)
	j got_regs
	
one_source:
	addi $t4, $0, 32 # if one source, $v1 is set to 32 and $v0 is set to whatever is in the rs section
	add $t3, $t0, $0
	srl $t3, $t3, 21 # bitmask for the rs section
	and $t3, $t3, 31 
	j got_regs
	
two_source:
	add $t3, $t0, $0
	srl $t3, $t3, 21 # shifting by 21 to get rs section
	and $t3, $t3, 31 # bitmask for the rs section but AFTER it was already shifter to the first 5 bits
	
	add $t4, $t0, $0
	srl $t4, $t4, 16 # shifting by 16 to get rt section
	and $t4, $t4, 31 # bitmask for the rt section but AFTER it was already shifter to the first 5 bits
	j got_regs
	

got_regs:
	addi $a0, $0, 3 # passes 3 as argument for step()
	jal step
	add $v0, $t3, $0 # put the extracted rs value (or no source value) into $v0
	add $v1, $t4, $0 # put the extract rt value (or one source value) into $v1
	lw $ra, 0($sp)
	lw $t0, 4($sp)
	lw $t1, 8($sp)
	lw $t2, 12($sp)
	lw $t3, 16($sp)
	lw $t4, 20($sp)
	addi $sp, $sp, 24 # popping registers off the stack and reducing the stack
	jr $ra


#############################################################
# get_next_pc
#############################################################
#############################################################
# Description:
# Writes step 4 since get_next_pc is the fourth step.
# Accepts an unsigned 32-bit instruction representing a MIPS machine word.
# Extracts the opcode from the current instruction and determines the type of instruction provided (R-type, J-type, I-type).
# If it's an R-type, we jump to a sequential branch and do PC + 4 to store in $v0. This also requires setting $v1 to 0xFFFFFFFF.
# If it's a J-type, we jump to a jump branch and calculate the target address using PC + 4 combined with 26 bits from our instruction. This is stored in $v0, and $v1 is set to 0xFFFFFFFF.
# If it's a I-type, we need to also check if it is a branch. If it's a branch, we calculate for (PC + 4) + (immediate * 4) and PC + 4, otherwise we just jump to sequential. These are stored in $v0 and $v1 respectively.
# If it's an invalid instruction, we jump to invalid instruction to set $v0 to 0xFFFFFFFF.
# Returns one or two integers as the byte addresses of the next instructions. Invalid instructions return 0xFFFFFFFF
#############################################################

.globl get_next_pc
get_next_pc:
    addi $sp, $sp, -4
    sw $ra, 0($sp)
    
    srl $t0, $a0, 26 # Extracting opcode from instruction
    
    # Determining where to send each opcode

    # If it's an R-format opcode, we know to only do PC + 4
    beq $t0, 0x00, sequential
    
    # If it's a J-format opcode, we only need the target address
    beq $t0, 0x02, jump
    beq $t0, 0x03, jump

    # If it's a I-format opcode, then we need to determine if it's a branch
    beq $t0, 0x05, branch_calc # Branching, so need to be calculated separately
    beq $t0, 0x08, sequential
    beq $t0, 0x23, sequential
    beq $t0, 0x2b, sequential
    
    j invalid_instruction
    
invalid_instruction:
    addi $t3, $0, 0xFFFFFFFF # Storing value in t3 to be added to $v0 later
    j done_branch 	     # Finished, jump to done_branch

sequential:
    addi $t3, $a1, 4 	     # Incrementing by 4
      li $t4, 0xFFFFFFFF     # Need to set $v1 to 0xFFFFFFFF
    j done_branch
    
jump:
    # handle j-type instruction
    sll $t2, $a0, 6          # Shift immediate field left by 6
    srl $t2, $t2, 4          # Shift back right by 4
    or $t3, $a1, $t2         # Add offset to current address
    li $t4, 0xFFFFFFFF       # Only have one source register, set t4 to appropriate hex
    j done_branch

branch_calc:
    sll $t2, $a0, 16         # Shift immediate field left by 16
    sra $t2, $t2, 14         # Shift back right by 14 (sign-extend)
    addi $t3, $a1, 4         # Address of next instruction (+8 bytes)
    add $t4, $a1, $t2        # Calculate branch target address
    addi $t4, $t4, 4         # Adjust target address for PC+4 behavior
    beq $t2, $zero, skip_branch  # Branch not taken
    move $t4, $t4            # Address of taken branch
    addi $t3, $t3, 0         # Address of not-taken branch
    j done_branch

skip_branch:
    addi $t3, $t3, 0         # Address of next sequential instruction
    addi $t4, $t4, 0         # Address of taken branch
    j done_branch

done_branch:
    addi $a0, $0, 4          # Making sure we are at step 4
    jal step
    add $v0, $t3, $0         # Add calculated address from $t3 into $v0
    add $v1, $t4, $0         # Add calculated address from $t4 into $v1 
    lw $ra, 0($sp)         
    addi $sp, $sp, 4 	     # Popping $ra off the stack and reducing the stack
    jr $ra

				



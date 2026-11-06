const { SlashCommandBuilder } = require('@discordjs/builders');

module.exports = {
	data: new SlashCommandBuilder()
		.setName('checkaccount')
		.setDescription('check an account')
		.addNumberOption(option =>
			option.setName('id')
				.setDescription('account id')
				.setRequired(true))

};
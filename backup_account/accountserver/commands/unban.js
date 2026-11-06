const { SlashCommandBuilder } = require('@discordjs/builders');

module.exports = {
	data: new SlashCommandBuilder()
		.setName('unban')
		.setDescription('unban account')
		.addNumberOption(option =>
			option.setName('id')
				.setDescription('account id')
				.setRequired(true))

};
const { SlashCommandBuilder } = require('@discordjs/builders');

module.exports = {
	data: new SlashCommandBuilder()
		.setName('auth')
		.setDescription('Link account to mope2020')
		.addStringOption(option =>
			option.setName('id')
				.setDescription('Your account id')
				.setRequired(true))
		.addStringOption(option =>
			option.setName('code')
				.setDescription('Your auth code')
				.setRequired(true))
};
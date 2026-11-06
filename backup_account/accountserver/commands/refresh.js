const { SlashCommandBuilder } = require('@discordjs/builders');

module.exports = {
	data: new SlashCommandBuilder()
		.setName('refresh')
		.setDescription('ADMIN: Refresh DB')
};